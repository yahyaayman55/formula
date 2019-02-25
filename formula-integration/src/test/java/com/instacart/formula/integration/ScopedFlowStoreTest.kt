package com.instacart.formula.integration

import arrow.core.Option
import com.google.common.truth.Truth.assertThat
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScopedFlowStoreTest {
    class SignUpComponent()
    class LoggedInComponent()

    sealed class Key {
        // Sign up
        object Login: Key()
        object Register: Key()

        // Logged in
        object Browse: Key()
        object Account: Key()
    }

    lateinit var keys: BehaviorRelay<ActiveKeys<Key>>
    lateinit var store: FlowStore<Key>

    // State relays
    lateinit var loginScreenState: BehaviorRelay<String>
    lateinit var registerScreenState: BehaviorRelay<String>
    lateinit var browseScreenState: BehaviorRelay<String>
    lateinit var accountScreenState: BehaviorRelay<String>

    lateinit var subscriber: TestSubscriber<Option<KeyState<Key, *>>>

    lateinit var disposed: MutableList<Any>


    @Before
    fun setup() {
        keys = BehaviorRelay.createDefault(ActiveKeys(emptyList()))
        loginScreenState = BehaviorRelay.createDefault("login-initial")
        registerScreenState = BehaviorRelay.createDefault("register-initial")
        browseScreenState = BehaviorRelay.createDefault("browse-initial")
        accountScreenState = BehaviorRelay.createDefault("account-initial")
        disposed = mutableListOf()

        store = FlowStore.init(keys.toFlowable(BackpressureStrategy.LATEST)) {
            withScope(scopeFactory = {
                val component = SignUpComponent()
                DisposableScope(
                    component = component,
                    onDispose = {
                        disposed.add(component)
                    }
                )
            }) {
                register(Key.Login::class, init = {
                    loginScreenState.toFlowable(BackpressureStrategy.LATEST)
                })

                register(Key.Register::class, init = {
                    registerScreenState.toFlowable(BackpressureStrategy.LATEST)
                })
            }

            withScope(scopeFactory = {
                val component = LoggedInComponent()
                DisposableScope(
                    component = component,
                    onDispose = {
                        disposed.add(component)
                    }
                )
            }) {
                register(Key.Browse::class, init = {
                    browseScreenState.toFlowable(BackpressureStrategy.LATEST)
                })

                register(Key.Account::class, init = {
                    accountScreenState.toFlowable(BackpressureStrategy.LATEST)
                })
            }
        }

        subscriber = TestSubscriber()
        store.screen().subscribe(subscriber)
    }

    @After
    fun cleanUp() {
        subscriber.dispose()
    }

    @Test
    fun disposeIsTriggered() {
        keys.accept(ActiveKeys(listOf(Key.Login)))
        keys.accept(ActiveKeys(listOf(Key.Browse)))

        assertThat(disposed).hasSize(1)
        disposed[0].let {
            assertThat(it).isInstanceOf(SignUpComponent::class.java)
        }
    }

    @Test
    fun allComponentsAreClearedOnDispose() {
        keys.accept(ActiveKeys(listOf(Key.Login)))
        keys.accept(
            ActiveKeys(
                listOf(
                    Key.Login,
                    Key.Browse
                )
            )
        )

        subscriber.dispose()

        assertThat(disposed).hasSize(2)
    }
}
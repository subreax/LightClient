package com.subreax.lightclient.data.state

import com.subreax.lightclient.data.state.Fsm.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


typealias FsmEventHandler<E> = Fsm<E>.(E) -> Unit

// Finite State Machine
// E - event id
class Fsm<E>(
    private val states: Array<State<E>>,
    initialState: Int
) {
    private val _stateId = MutableStateFlow(initialState)
    val stateId: StateFlow<Int>
        get() = _stateId


    fun setState(stateId: Int) {
        if (stateId < states.size) {
            _stateId.value = stateId
        } else {
            throw IndexOutOfBoundsException("State id is out of bounds. Max is ${states.size - 1} but actual is $stateId")
        }
    }

    fun notifyEvent(event: E) {
        val state = states[_stateId.value]
        state.onEvent(this, event)
    }


    data class State<E>(
        val onEvent: FsmEventHandler<E>
    )

    class Builder<E> {
        data class State<E>(
            val id: Int,
            val onEvent: FsmEventHandler<E>
        )

        private val states = mutableListOf<State<E>>()

        fun addState(
            id: Int,
            onEvent: FsmEventHandler<E>
        ): Builder<E> {
            states.add(State(id, onEvent))
            return this
        }

        fun build(initialStateId: Int): Fsm<E> {
            states.sortBy { it.id }

            if (states.isNotEmpty()) {
                // todo: check ids
                return Fsm(
                    states.map { State(it.onEvent) }.toTypedArray(),
                    initialStateId
                )
            }

            throw IllegalStateException("No states")
        }
    }
}
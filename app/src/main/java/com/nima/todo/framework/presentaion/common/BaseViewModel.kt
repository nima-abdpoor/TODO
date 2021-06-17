package com.nima.todo.framework.presentaion.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nima.todo.business.data.util.GenericError.INVALID_STATE_EVENT
import com.nima.todo.business.domain.state.*
import com.nima.todo.business.domain.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


@FlowPreview
@ExperimentalCoroutinesApi
abstract class BaseViewModel<ViewState> : ViewModel()
{
    private val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    private val dataChannelManager: DataChannelManager<ViewState>
            = object: DataChannelManager<ViewState>(){

        override fun handleNewData(data: ViewState) {
            this@BaseViewModel.handleNewData(data)
        }
    }

    val viewState: LiveData<ViewState>
        get() = _viewState

    val shouldDisplayProgressBar: LiveData<Boolean>
            = dataChannelManager.shouldDisplayProgressBar

    val stateMessage: LiveData<StateMessage?>
        get() = dataChannelManager.messageStack.stateMessage

    // FOR DEBUGGING
    fun getMessageStackSize(): Int{
        return dataChannelManager.messageStack.size
    }

    fun setupChannel() = dataChannelManager.setupChannel()

    abstract fun handleNewData(data: ViewState)

    abstract fun setStateEvent(stateEvent: StateEvent)

    fun emitStateMessageEvent(
        stateMessage: StateMessage,
        stateEvent: StateEvent
    ) = flow{
        emit(
            DataState.error<ViewState>(
                response = stateMessage.response,
                stateEvent = stateEvent
            )
        )
    }

    fun emitInvalidStateEvent(stateEvent: StateEvent) = flow {
        emit(
            DataState.error<ViewState>(
                response = Response(
                    message = INVALID_STATE_EVENT,
                    uiComponentType = UIComponentType.None(),
                    messageType = MessageType.Error()
                ),
                stateEvent = stateEvent
            )
        )
    }

    fun launchJob(
        stateEvent: StateEvent,
        jobFunction: Flow<DataState<ViewState>?>
    ) = dataChannelManager.launchJob(stateEvent, jobFunction)

    fun getCurrentViewStateOrNew(): ViewState{
        return viewState.value ?: initNewViewState()
    }

    fun setViewState(viewState: ViewState){
        _viewState.value = viewState
    }

    fun clearStateMessage(index: Int = 0){
        printLogD("BaseViewModel", "clearStateMessage")
        dataChannelManager.clearStateMessage(index)
    }

    fun clearActiveStateEvents() = dataChannelManager.clearActiveStateEventCounter()

    fun clearAllStateMessages() = dataChannelManager.clearAllStateMessages()

    fun printStateMessages() = dataChannelManager.printStateMessages()

    fun cancelActiveJobs() = dataChannelManager.cancelJobs()

    abstract fun initNewViewState(): ViewState

}

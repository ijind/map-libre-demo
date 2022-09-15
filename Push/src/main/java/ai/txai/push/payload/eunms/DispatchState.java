package ai.txai.push.payload.eunms;

/**
 * Time: 17/03/2022
 * Author Hay
 */
public enum DispatchState {
    Pending,
    Dispatched,
    Cancelled,
    Arriving,
    Arrived,
    Charging,
    Finished;
}

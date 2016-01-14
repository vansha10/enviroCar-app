package org.envirocar.obd.commands.response.quirks;

import org.envirocar.obd.commands.response.DataResponse;

public interface ResponseQuirk<T extends DataResponse> {

    T preProcess(T original, int[] rawBytes);

}

package de.huntertagog.locobroko.api;

import lombok.Getter;

public class CoinAPI {

    @Getter
    private static ICoinAPI api;

    public static void setApi(ICoinAPI api) {
        CoinAPI.api = api;
    }
}

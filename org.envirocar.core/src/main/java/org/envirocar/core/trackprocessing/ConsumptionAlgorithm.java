/**
 * Copyright (C) 2013 - 2015 the enviroCar community
 * <p>
 * This file is part of the enviroCar app.
 * <p>
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.core.trackprocessing;


import org.envirocar.core.entity.Measurement;
import org.envirocar.core.exception.FuelConsumptionException;
import org.envirocar.core.exception.UnsupportedFuelTypeException;
import org.envirocar.core.logging.Logger;

import rx.Observable;
import rx.Subscriber;

/**
 * TODO JavaDoc
 *
 * @author dewall
 */
public abstract class ConsumptionAlgorithm implements Observable.Operator<Measurement, Measurement> {
    private static final Logger LOG = Logger.getLogger(ConsumptionAlgorithm.class);

    /**
     * An implementation shall calculate the fuel consumption (l/h).
     *
     * @param measurement the measurement providing the required parameters
     * @return fuel consumption in l/h
     * @throws FuelConsumptionException if required parameters were missing
     * @throws UnsupportedFuelTypeException
     */
    abstract double calculateConsumption(Measurement measurement) throws
            FuelConsumptionException, UnsupportedFuelTypeException;

    /**
     * An implementation shall calculate the CO2 emission (kg/h) for a fuel consumption value (l/h)
     *
     * @param consumption fuel consumption in l/h
     * @return CO2 emission in kg/h
     * @throws FuelConsumptionException if the fuelType is not supported
     */
    abstract double calculateCO2FromConsumption(double consumption) throws
            FuelConsumptionException;

    @Override
    public Subscriber<? super Measurement> call(Subscriber<? super Measurement> subscriber) {
        return new Subscriber<Measurement>() {
            @Override
            public void onCompleted() {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }

            @Override
            public void onNext(Measurement measurement) {
                if (subscriber.isUnsubscribed())
                    return;

                try {
                    double consumption = calculateConsumption(measurement);
                    double co2 = calculateCO2FromConsumption(consumption);
                    measurement.setProperty(Measurement.PropertyKey.CONSUMPTION, consumption);
                    measurement.setProperty(Measurement.PropertyKey.CO2, co2);
                } catch (FuelConsumptionException e) {
                    LOG.warn(e.getMessage());
                } catch (UnsupportedFuelTypeException e) {
                    LOG.warn(e.getMessage());
                }

                subscriber.onNext(measurement);
            }
        };
    }
}

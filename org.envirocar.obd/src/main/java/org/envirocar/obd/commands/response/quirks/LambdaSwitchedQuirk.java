package org.envirocar.obd.commands.response.quirks;

import org.envirocar.core.logging.Logger;
import org.envirocar.obd.commands.response.entity.LambdaProbeVoltageResponse;

public class LambdaSwitchedQuirk implements ResponseQuirk<LambdaProbeVoltageResponse> {

    private static final Logger LOGGER = Logger.getLogger(LambdaSwitchedQuirk.class);

    private int totalLambdas;
    private int lambdaSwitchCandidates;
    private SwitchedState state;

    enum SwitchedState {
        DETERMINATION,
        SWITCH_REQUIRED,
        ORIGINAL
    }

    public LambdaSwitchedQuirk() {
        this(SwitchedState.DETERMINATION);
    }

    public LambdaSwitchedQuirk(SwitchedState s) {
        this.state = s;
    }

    @Override
    public LambdaProbeVoltageResponse preProcess(LambdaProbeVoltageResponse lambda, int[] rawBytes) {
        if (state == SwitchedState.DETERMINATION) {
            totalLambdas++;
            /**
             * we are in determination mode
             */
            double ratio = lambda.getEquivalenceRatio() == 0.0d ? 1.0d : (lambda.getVoltage() / lambda.getEquivalenceRatio());

            if (ratio >= 1.0d) {
                lambdaSwitchCandidates++;
            }

            if (totalLambdas > 100) {
                if (lambdaSwitchCandidates > 20) {
                    state = SwitchedState.SWITCH_REQUIRED;
                }
                else {
                    state = SwitchedState.ORIGINAL;
                }

                LOGGER.info("Lambda Switch analysis completed: "+state);
            }

            return lambda;
        }
        else {
            if (state == SwitchedState.SWITCH_REQUIRED) {
                /**
                 * there are two ways of switching lambda response values:
                 *
                 * 1. just switch the calculated results (this does not consider the different formulas for ER and V)
                 * 2. switch the byte position 2,3 and 4,5 and recalculate with the formulas
                 *
                 * for the moment, we just do (1)
                 */
                return new LambdaProbeVoltageResponse(lambda.getEquivalenceRatio(), lambda.getVoltage());
            }
            else {
                return lambda;
            }
        }
    }

}

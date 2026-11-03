package dev.abysslasea.immersedleaves.client;

import java.util.Random;

/** Moves overlapping leaf clusters around local stems, with separate side tilt and lift. */
public final class LeafTextureMapping {
    private static final int GRID = 2;
    private static final int CLUMPS = GRID * GRID;
    private static final double[] WIND_CURVE = {0.0, 0.35, 0.72, 1.0, 0.6, 0.1, -0.48, -0.3};
    private final Layout layout;
    private final double[] position = new double[CLUMPS * 3];
    private final double[] velocity = new double[CLUMPS * 3];
    private final double[] diagonal = new double[CLUMPS];
    private final double[] lean = new double[CLUMPS];
    private final double[] tilt = new double[CLUMPS];
    private final double[] lift = new double[CLUMPS];
    private final double breezeOffset;
    private double time;
    private double breezeTime;

    public LeafTextureMapping(int width, int height, int seed, double phaseSeconds) {
        this(new Layout(width, height, 32 - Integer.numberOfLeadingZeros(Math.min(width, height)), seed), phaseSeconds);
    }

    LeafTextureMapping(Layout layout, double phaseSeconds) {
        this.layout = layout;
        time = phaseSeconds;
        breezeOffset = phaseSeconds / 36.0 * 1.4 + layout.phaseOffset * 0.5;
    }

    public void advance(double seconds, float strength, float gust) {
        double wind = Math.min(2.5, Math.max(0.0, strength));
        double gustAmount = Math.min(1.5, Math.max(0.0, gust));
        double amplitude = Math.min(1.0, wind * 4.0) * (0.65 + wind * 0.95 + gustAmount * 0.45);
        double calm = Math.max(0.0, 1.0 - Math.max(0.0, wind - 0.2) / 0.45 - gustAmount * 2.0);
        // The idle input runs at 20% speed; count its breeze cycle in real seconds.
        breezeTime = (breezeTime + seconds * 5.0) % 12.0;
        seconds *= 1.0 - calm * 0.6;
        time = (time + seconds) % 36.0;
        seconds *= 1.0 + calm * 2.75;
        double damping = 4.5 - calm * 2.1;
        double stiffness = 64.0 - calm * 48.0;
        double decay = Math.exp(-damping * seconds);
        double frequency = Math.sqrt(stiffness - damping * damping);
        double c = Math.cos(frequency * seconds);
        double s = Math.sin(frequency * seconds) / frequency;
        for (int clump = 0; clump < CLUMPS; clump++) {
            double phase = layout.phaseOffset - layout.lag[clump];
            double detailPhase = phase + layout.flutterPhase[clump];
            double reach = amplitude * layout.gain[clump];
            // Nearby leaf clusters follow the breeze with small delays and a gentler rise and fall.
            double localBreeze = (breezeTime - breezeOffset - layout.lag[clump] * 2.0 + 12.0) % 12.0;
            double pulse = localBreeze < 5.5 ? Math.sin(Math.PI * localBreeze / 5.5) : 0.0;
            double pulseSquared = pulse * pulse;
            double idleReach = calm * pulseSquared * (2.0 - pulseSquared);
            double bend = reach * ((1.0 - calm) * (0.15 * curve(time / 6.0 + phase)
                    + 0.035 * curve(time / 2.4 + detailPhase))
                    + idleReach * (0.18 + 0.025 * curve(time / 6.0 + detailPhase)));
            double sideTilt = reach * ((1.0 - calm) * 0.075 * curve(time / 4.5 + detailPhase)
                    + idleReach * 0.045 * curve(time / 4.5 + detailPhase));
            double localLift = reach * ((1.0 - calm) * 0.085 * curve(time / 3.0 + detailPhase + 0.23)
                    + idleReach * 0.035 * curve(time / 3.0 + detailPhase + 0.23));
            for (int axis = 0; axis < 3; axis++) {
                int index = clump * 3 + axis;
                double target = axis == 0 ? bend : axis == 1 ? sideTilt : localLift;
                double displacement = position[index] - target;
                double v = velocity[index];
                position[index] = target + decay * (displacement * c + (v + damping * displacement) * s);
                velocity[index] = decay * (v * c - (damping * v + stiffness * displacement) * s);
            }
            double angle = position[clump * 3];
            double skew = position[clump * 3 + 1];
            double sine = Math.sin(angle);
            diagonal[clump] = Math.cos(angle) - 1.0;
            lean[clump] = sine - skew * 0.3;
            tilt[clump] = -sine + skew;
            lift[clump] = position[clump * 3 + 2];
        }
    }

    public int sourceIndex(int x, int y, int level) {
        Layer layer = layout.layers[level];
        int offset = (y * layer.width + x) * CLUMPS;
        double dx = 0.0;
        double dy = 0.0;
        for (int clump = 0; clump < CLUMPS; clump++) {
            int index = offset + clump;
            double localX = layer.localX[index];
            double localY = layer.localY[index];
            dx += diagonal[clump] * localX + lean[clump] * localY;
            dy += tilt[clump] * localX + diagonal[clump] * localY + lift[clump] * layer.weight[index];
        }
        int sx = Math.floorMod(x + (int) Math.floor(dx * layer.width / GRID + 0.5), layer.width);
        int sy = Math.floorMod(y + (int) Math.floor(dy * layer.height / GRID + 0.5), layer.height);
        return sy * layer.width + sx;
    }

    /** Returns whole source-pixel coordinates, without an enlarged canvas or color interpolation. */
    public void map(double x, double y, double[] source) {
        Layer layer = layout.layers[0];
        int index = sourceIndex(Math.floorMod((int) Math.floor(x + 0.5), layer.width),
                Math.floorMod((int) Math.floor(y + 0.5), layer.height), 0);
        source[0] = index % layer.width;
        source[1] = index / layer.width;
    }

    private static double curve(double phase) {
        double frame = (phase - Math.floor(phase)) * WIND_CURVE.length;
        int index = (int) frame;
        double blend = frame - index;
        blend = blend * blend * (3.0 - 2.0 * blend);
        return WIND_CURVE[index] * (1.0 - blend) + WIND_CURVE[(index + 1) % WIND_CURVE.length] * blend;
    }

    /** Immutable sampling geometry is shared by every animation phase of a source texture. */
    static final class Layout {
        final double phaseOffset;
        final double[] lag = new double[CLUMPS];
        final double[] gain = new double[CLUMPS];
        final double[] flutterPhase = new double[CLUMPS];
        final Layer[] layers;

        Layout(int width, int height, int levels, int seed) {
            Random random = new Random(seed * 0x9E3779B97F4A7C15L);
            phaseOffset = random.nextDouble();
            double[] pivotX = new double[CLUMPS];
            double[] pivotY = new double[CLUMPS];
            for (int clump = 0; clump < CLUMPS; clump++) {
                lag[clump] = clump % GRID * 0.09 + random.nextDouble() * 0.12;
                gain[clump] = 0.85 + random.nextDouble() * 0.3;
                flutterPhase[clump] = random.nextDouble();
                pivotX[clump] = (random.nextDouble() - 0.5) * 0.3;
                pivotY[clump] = 0.3 + random.nextDouble() * 0.15;
            }
            layers = new Layer[levels];
            for (int level = 0; level < levels; level++) {
                Layer layer = new Layer(width >> level, height >> level);
                layers[level] = layer;
                for (int y = 0; y < layer.height; y++) {
                    double gridY = (y + 0.5) * GRID / layer.height - 0.5;
                    int row = (int) Math.floor(gridY);
                    double fractionY = gridY - row;
                    double blendY = fractionY * fractionY * (3.0 - 2.0 * fractionY);
                    for (int x = 0; x < layer.width; x++) {
                        double gridX = (x + 0.5) * GRID / layer.width - 0.5;
                        int column = (int) Math.floor(gridX);
                        double fractionX = gridX - column;
                        double blendX = fractionX * fractionX * (3.0 - 2.0 * fractionX);
                        int offset = (y * layer.width + x) * CLUMPS;
                        for (int iy = 0; iy < 2; iy++) {
                            for (int ix = 0; ix < 2; ix++) {
                                int clump = Math.floorMod(row + iy, GRID) * GRID + Math.floorMod(column + ix, GRID);
                                int index = offset + clump;
                                double weight = (ix == 0 ? 1.0 - blendX : blendX) * (iy == 0 ? 1.0 - blendY : blendY);
                                layer.weight[index] = (float) weight;
                                layer.localX[index] = (float) (weight * (fractionX - ix - pivotX[clump]));
                                layer.localY[index] = (float) (weight * (fractionY - iy - pivotY[clump]));
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class Layer {
        final int width;
        final int height;
        final float[] weight;
        final float[] localX;
        final float[] localY;

        Layer(int width, int height) {
            this.width = width;
            this.height = height;
            weight = new float[width * height * CLUMPS];
            localX = new float[weight.length];
            localY = new float[weight.length];
        }
    }
}

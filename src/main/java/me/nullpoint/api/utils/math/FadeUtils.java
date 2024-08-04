package me.nullpoint.api.utils.math;

public class FadeUtils {
    private long start;
    public long length;

    public FadeUtils(final long ms) {
        this.length = ms;
        this.reset();
    }

    public FadeUtils reset() {
        this.start = System.currentTimeMillis();
        return this;
    }

    public boolean isEnd() {
        return this.getTime() >= this.length;
    }

    protected long getTime() {
        return System.currentTimeMillis() - this.start;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public double getFadeOne() {
        return this.isEnd() ? 1.0 : (this.getTime() / (double) this.length);
    }

    public double getFadeInDefault() {
        return Math.tanh(this.getTime() / (double) this.length * 3.0);
    }

    public double getFadeOutDefault() {
        return 1.0 - Math.tanh(this.getTime() / (double) this.length * 3.0);
    }

    public double getEpsEzFadeIn() {
        return 1.0 - Math.sin(1.5707963267948966 * this.getFadeOne()) * Math.sin(2.5132741228718345 * this.getFadeOne());
    }

    public double getEpsEzFadeOut() {
        return Math.sin(1.5707963267948966 * this.getFadeOne()) * Math.sin(2.5132741228718345 * this.getFadeOne());
    }

    public double easeOutQuad() {
        return 1.0 - (1.0 - this.getFadeOne()) * (1.0 - this.getFadeOne());
    }

    public double easeInQuad() {
        return this.getFadeOne() * this.getFadeOne();
    }

    public double getQuad(Quad quad) {
        switch (quad) {
            case In -> {
                return easeInQuad();
            }
            case In2 -> {
                return getFadeInDefault();
            }
            case Out -> {
                return easeOutQuad();
            }
        }
        return easeOutQuad();
    }
    public enum Quad {
        In,
        In2,
        Out,
    }
}

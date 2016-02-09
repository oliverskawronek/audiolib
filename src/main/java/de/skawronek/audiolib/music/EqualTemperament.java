package de.skawronek.audiolib.music;

import java.text.DecimalFormat;

import org.eclipse.jdt.annotation.NonNull;

import de.skawronek.audiolib.util.Util;

public final class EqualTemperament {
	private static final double DEFAULT_TUNING_FREQUENCY = 440; // Hz

	private static final EqualTemperament INSTANCE = new EqualTemperament();

	/**
	 * Frequenz des Kammertons (engl. Concert Pitch) a' in Hz
	 */
	private volatile double tuningFrequency = DEFAULT_TUNING_FREQUENCY;

	private EqualTemperament() {
	}

	public static @NonNull EqualTemperament getInstance() {
		return INSTANCE;
	}

	public void setTuningFrequency(final double tuningFrequency) {
		if (tuningFrequency <= 0.0f) {
			throw new IllegalArgumentException("tuningFrequency "
					+ tuningFrequency + " <= 0");
		}

		this.tuningFrequency = tuningFrequency;
	}

	public double getTuningFrequency() {
		return tuningFrequency;
	}

	public double getFrequencyOf(final @NonNull Pitch pitch) {
		// Eine cached Variante mit ConcurrentHashMap hat keinerlei
		// Performance-Zuwachs gebracht
		final int p = pitch.getSemitoneIndex();
		final double frequency = tuningFrequency * Math.pow(2, (p - 69) / 12.0);
		return frequency;
	}

	public @NonNull Pitch getClosestPitchTo(final double frequency,
			final @NonNull Accidental preferedAccidental) {
		if (frequency <= 0) {
			throw new IllegalArgumentException("frequency " + frequency
					+ " <= 0");
		}

		// Semitone-Index
		final int p = (int) Math.round(69 + 12 * Util.log2(frequency
				/ tuningFrequency));
		return Pitch.fromSemitoneIndex(p, preferedAccidental);
	}

	private static final DecimalFormat FORMATTER = new DecimalFormat("0.##");

	@Override
	public @NonNull String toString() {
		return "EqualTemperament[tuningFrequency: "
				+ FORMATTER.format(tuningFrequency) + "Hz]";
	}

	/**
	 * Gibt <code>true</code> zurück, wenn <code>p1</code> und <code>p2</code>
	 * enharmonisch verwechselt werden können. In der gleichstufigen Stimmung
	 * haben dann beide Pitches die selbe Tonhöhe.
	 * 
	 * @param p1
	 * @param p2
	 * @return <code>true</code>, wenn <code>p1</code> und <code>p2</code>
	 *         enharmonisch verwechselt werden können
	 */
	public static boolean areEnharmonicTogether(final @NonNull Pitch p1,
			final @NonNull Pitch p2) {
		return p1.getSemitoneIndex() == p2.getSemitoneIndex();
	}

	/**
	 * Gibt das Tonhöhenintervall von <code>f1</code> zu <code>f2</code> in Cent
	 * zurück. Dabei entsprechen +100 Cent einem Halbtonsprung nach oben.
	 * 
	 * @param f1
	 *            Ausgangsfrequenz
	 * @param f2
	 *            Zielfrequenz
	 * @return Tonhöhenintervall von <code>f1</code> zu <code>f2</code> in Cent.
	 */
	public static double getCentInterval(final double f1, final double f2) {
		if (Double.isNaN(f1) || Double.isNaN(f2) || Double.isInfinite(f1)
				|| Double.isInfinite(f2) || f1 == 0.0d || f2 == 0.0d) {
			throw new IllegalArgumentException();
		}

		return 1200.0d * (Math.log(f2 / f1) / Math.log(2));
	}

	/**
	 * Transponiert die Frequenz <code>freq</code> um <code>cent</code>. Dabei
	 * entsprechen +100 Cent einer Erhöhung um einen Halbton. Es sind auch
	 * negative Cent-Werte zulässig, bspw. verringert -100 Cent die Frequenz um
	 * einen Halbton.
	 * 
	 * @param freq
	 *            Ausgangsfrequenz in Hz
	 * @param cent
	 *            Tonhöhenintervall
	 * @return Transponierte Frequenz
	 */
	public static double transpose(final double freq, final double cent) {
		if (freq <= 0) {
			throw new IllegalArgumentException("freq " + freq + " <= 0");
		}

		return freq / Math.exp((-cent * Math.log(2d)) / 1200d);
	}
}

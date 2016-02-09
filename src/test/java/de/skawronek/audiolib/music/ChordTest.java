package de.skawronek.audiolib.music;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;

import static org.junit.Assert.*;
import de.skawronek.audiolib.music.Chord.Type;

public class ChordTest {
	@Test
	public void testMajorChordPitches() {
		final Chord cMaj = new Chord(Pitch.fromString("C3"), Type.MAJOR);
		assertChordContainsPitches(cMaj, new String[] { "C3", "E3", "G3" });

		final Chord cSharpMaj = new Chord(Pitch.fromString("C#3"), Type.MAJOR);
		assertChordContainsPitches(cSharpMaj, new String[] { "C#3", "E#3",
				"G#3" });

		final Chord dMaj = new Chord(Pitch.fromString("D3"), Type.MAJOR);
		assertChordContainsPitches(dMaj, new String[] { "D3", "F#3", "A3" });

		final Chord eFlatMaj = new Chord(Pitch.fromString("Eb3"), Type.MAJOR);
		assertChordContainsPitches(eFlatMaj,
				new String[] { "Eb3", "G3", "Bb3" });

		final Chord eMaj = new Chord(Pitch.fromString("E3"), Type.MAJOR);
		assertChordContainsPitches(eMaj, new String[] { "E3", "G#3", "B3" });

		final Chord fMaj = new Chord(Pitch.fromString("F3"), Type.MAJOR);
		assertChordContainsPitches(fMaj, new String[] { "F3", "A3", "C4" });

		final Chord fSharpMaj = new Chord(Pitch.fromString("F#3"), Type.MAJOR);
		assertChordContainsPitches(fSharpMaj, new String[] { "F#3", "A#3",
				"C#4" });

		final Chord gMaj = new Chord(Pitch.fromString("G3"), Type.MAJOR);
		assertChordContainsPitches(gMaj, new String[] { "G3", "B3", "D4" });

		final Chord aFlatMaj = new Chord(Pitch.fromString("Ab3"), Type.MAJOR);
		assertChordContainsPitches(aFlatMaj,
				new String[] { "Ab3", "C4", "Eb4" });

		final Chord aMaj = new Chord(Pitch.fromString("A3"), Type.MAJOR);
		assertChordContainsPitches(aMaj, new String[] { "A3", "C#4", "E4" });

		final Chord bFlatMaj = new Chord(Pitch.fromString("Bb3"), Type.MAJOR);
		assertChordContainsPitches(bFlatMaj, new String[] { "Bb3", "D4", "F4" });

		final Chord bMaj = new Chord(Pitch.fromString("B3"), Type.MAJOR);
		assertChordContainsPitches(bMaj, new String[] { "B3", "D#4", "F#4" });
	}

	@Test
	public void testMinorChordPitches() {
		final Chord cMin = new Chord(Pitch.fromString("C3"), Type.MINOR);
		assertChordContainsPitches(cMin, new String[] { "C3", "Eb3", "G3" });

		final Chord cSharpMin = new Chord(Pitch.fromString("C#3"), Type.MINOR);
		assertChordContainsPitches(cSharpMin,
				new String[] { "C#3", "E3", "G#3" });

		final Chord dMin = new Chord(Pitch.fromString("D3"), Type.MINOR);
		assertChordContainsPitches(dMin, new String[] { "D3", "F3", "A3" });

		final Chord eFlatMin = new Chord(Pitch.fromString("Eb3"), Type.MINOR);
		assertChordContainsPitches(eFlatMin,
				new String[] { "Eb3", "Gb3", "Bb3" });

		final Chord eMin = new Chord(Pitch.fromString("E3"), Type.MINOR);
		assertChordContainsPitches(eMin, new String[] { "E3", "G3", "B3" });

		final Chord fMin = new Chord(Pitch.fromString("F3"), Type.MINOR);
		assertChordContainsPitches(fMin, new String[] { "F3", "Ab3", "C4" });

		final Chord fSharpMin = new Chord(Pitch.fromString("F#3"), Type.MINOR);
		assertChordContainsPitches(fSharpMin,
				new String[] { "F#3", "A3", "C#4" });

		final Chord gMin = new Chord(Pitch.fromString("G3"), Type.MINOR);
		assertChordContainsPitches(gMin, new String[] { "G3", "Bb3", "D4" });

		final Chord aFlatMin = new Chord(Pitch.fromString("Ab3"), Type.MINOR);
		assertChordContainsPitches(aFlatMin,
				new String[] { "Ab3", "Cb4", "Eb4" });

		final Chord aMin = new Chord(Pitch.fromString("A3"), Type.MINOR);
		assertChordContainsPitches(aMin, new String[] { "A3", "C4", "E4" });

		final Chord bFlatMin = new Chord(Pitch.fromString("Bb3"), Type.MINOR);
		assertChordContainsPitches(bFlatMin,
				new String[] { "Bb3", "Db4", "F4" });

		final Chord bMin = new Chord(Pitch.fromString("B3"), Type.MINOR);
		assertChordContainsPitches(bMin, new String[] { "B3", "D4", "F#4" });
	}

	@Test
	public void testAugmenetedChordPitches() {
		final Chord cAug = new Chord(Pitch.fromString("C3"), Type.AUGMENTED);
		assertChordContainsPitches(cAug, new String[] { "C3", "E3", "G#3" });

		final Chord cSharpAug = new Chord(Pitch.fromString("C#3"),
				Type.AUGMENTED);
		assertChordContainsPitches(cSharpAug,
				new String[] { "C#3", "E#3", "A3" });

		final Chord dAug = new Chord(Pitch.fromString("D3"), Type.AUGMENTED);
		assertChordContainsPitches(dAug, new String[] { "D3", "F#3", "A#3" });

		final Chord eFlatAug = new Chord(Pitch.fromString("Eb3"),
				Type.AUGMENTED);
		assertChordContainsPitches(eFlatAug, new String[] { "Eb3", "G3", "B3" });

		final Chord eAug = new Chord(Pitch.fromString("E3"), Type.AUGMENTED);
		assertChordContainsPitches(eAug, new String[] { "E3", "G#3", "C4" });

		final Chord fAug = new Chord(Pitch.fromString("F3"), Type.AUGMENTED);
		assertChordContainsPitches(fAug, new String[] { "F3", "A3", "C#4" });

		final Chord fSharpAug = new Chord(Pitch.fromString("F#3"),
				Type.AUGMENTED);
		assertChordContainsPitches(fSharpAug,
				new String[] { "F#3", "A#3", "D4" });

		final Chord gAug = new Chord(Pitch.fromString("G3"), Type.AUGMENTED);
		assertChordContainsPitches(gAug, new String[] { "G3", "B3", "D#4" });

		final Chord aFlatAug = new Chord(Pitch.fromString("Ab3"),
				Type.AUGMENTED);
		assertChordContainsPitches(aFlatAug, new String[] { "Ab3", "C4", "E4" });

		final Chord aAug = new Chord(Pitch.fromString("A3"), Type.AUGMENTED);
		assertChordContainsPitches(aAug, new String[] { "A3", "C#4", "F4" });

		final Chord bFlatAug = new Chord(Pitch.fromString("Bb3"),
				Type.AUGMENTED);
		assertChordContainsPitches(bFlatAug,
				new String[] { "Bb3", "D4", "F#4" });

		final Chord bAug = new Chord(Pitch.fromString("B3"), Type.AUGMENTED);
		assertChordContainsPitches(bAug, new String[] { "B3", "D#4", "G4" });
	}

	@Test
	public void testDiminishedChordPitches() {
		final Chord cDim = new Chord(Pitch.fromString("C3"), Type.DIMINISHED);
		assertChordContainsPitches(cDim, new String[] { "C3", "Eb3", "Gb3" });

		final Chord cSharpDim = new Chord(Pitch.fromString("C#3"),
				Type.DIMINISHED);
		assertChordContainsPitches(cSharpDim,
				new String[] { "C#3", "E3", "G3" });

		final Chord dDim = new Chord(Pitch.fromString("D3"), Type.DIMINISHED);
		assertChordContainsPitches(dDim, new String[] { "D3", "F3", "Ab3" });

		final Chord eFlatDim = new Chord(Pitch.fromString("Eb3"),
				Type.DIMINISHED);
		assertChordContainsPitches(eFlatDim,
				new String[] { "Eb3", "Gb3", "A3" });

		final Chord eDim = new Chord(Pitch.fromString("E3"), Type.DIMINISHED);
		assertChordContainsPitches(eDim, new String[] { "E3", "G3", "Bb3" });

		final Chord fDim = new Chord(Pitch.fromString("F3"), Type.DIMINISHED);
		assertChordContainsPitches(fDim, new String[] { "F3", "Ab3", "B3" });

		final Chord fSharpDim = new Chord(Pitch.fromString("F#3"),
				Type.DIMINISHED);
		assertChordContainsPitches(fSharpDim,
				new String[] { "F#3", "A3", "C4" });

		final Chord gDim = new Chord(Pitch.fromString("G3"), Type.DIMINISHED);
		assertChordContainsPitches(gDim, new String[] { "G3", "Bb3", "Db4" });

		final Chord aFlatDim = new Chord(Pitch.fromString("Ab3"),
				Type.DIMINISHED);
		assertChordContainsPitches(aFlatDim,
				new String[] { "Ab3", "Cb4", "D4" });

		final Chord aDim = new Chord(Pitch.fromString("A3"), Type.DIMINISHED);
		assertChordContainsPitches(aDim, new String[] { "A3", "C4", "Eb4" });

		final Chord bFlatDim = new Chord(Pitch.fromString("Bb3"),
				Type.DIMINISHED);
		assertChordContainsPitches(bFlatDim,
				new String[] { "Bb3", "Db4", "E4" });

		final Chord bDim = new Chord(Pitch.fromString("B3"), Type.DIMINISHED);
		assertChordContainsPitches(bDim, new String[] { "B3", "D4", "F4" });
	}

	@Test
	public void testDominantSeventhChordPitches() {
		final Chord cDom7 = new Chord(Pitch.fromString("C3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(cDom7, new String[] { "C3", "E3", "G3",
				"Bb3" });

		final Chord cSharpDom7 = new Chord(Pitch.fromString("C#3"),
				Type.DOMINANT_7TH);
		assertChordContainsPitches(cSharpDom7, new String[] { "C#3", "E#3",
				"G#3", "B3" });

		final Chord dDom7 = new Chord(Pitch.fromString("D3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(dDom7, new String[] { "D3", "F#3", "A3",
				"C4" });

		final Chord eFlatDom7 = new Chord(Pitch.fromString("Eb3"),
				Type.DOMINANT_7TH);
		assertChordContainsPitches(eFlatDom7, new String[] { "Eb3", "G3",
				"Bb3", "Db4" });

		final Chord eDom7 = new Chord(Pitch.fromString("E3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(eDom7, new String[] { "E3", "G#3", "B3",
				"D4" });

		final Chord fDom7 = new Chord(Pitch.fromString("F3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(fDom7, new String[] { "F3", "A3", "C4",
				"Eb4" });

		final Chord fSharpDom7 = new Chord(Pitch.fromString("F#3"),
				Type.DOMINANT_7TH);
		assertChordContainsPitches(fSharpDom7, new String[] { "F#3", "A#3",
				"C#4", "E4" });

		final Chord gDom7 = new Chord(Pitch.fromString("G3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(gDom7,
				new String[] { "G3", "B3", "D4", "F4" });

		final Chord aFlatDom7 = new Chord(Pitch.fromString("Ab3"),
				Type.DOMINANT_7TH);
		assertChordContainsPitches(aFlatDom7, new String[] { "Ab3", "C4",
				"Eb4", "Gb4" });

		final Chord aDom7 = new Chord(Pitch.fromString("A3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(aDom7, new String[] { "A3", "C#4", "E4",
				"G4" });

		final Chord bFlatDom7 = new Chord(Pitch.fromString("Bb3"),
				Type.DOMINANT_7TH);
		assertChordContainsPitches(bFlatDom7, new String[] { "Bb3", "D4", "F4",
				"Ab4" });

		final Chord bDom7 = new Chord(Pitch.fromString("B3"), Type.DOMINANT_7TH);
		assertChordContainsPitches(bDom7, new String[] { "B3", "D#4", "F#4",
				"A4" });
	}

	@Test
	public void testMajorSeventhChordPitches() {
		final Chord cMaj7 = new Chord(Pitch.fromString("C3"), Type.MAJOR_7TH);
		assertChordContainsPitches(cMaj7,
				new String[] { "C3", "E3", "G3", "B3" });

		final Chord cSharpMaj7 = new Chord(Pitch.fromString("C#3"),
				Type.MAJOR_7TH);
		assertChordContainsPitches(cSharpMaj7, new String[] { "C#3", "E#3",
				"G#3", "B#3" });

		final Chord dMaj7 = new Chord(Pitch.fromString("D3"), Type.MAJOR_7TH);
		assertChordContainsPitches(dMaj7, new String[] { "D3", "F#3", "A3",
				"C#4" });

		final Chord eFlatMaj7 = new Chord(Pitch.fromString("Eb3"),
				Type.MAJOR_7TH);
		assertChordContainsPitches(eFlatMaj7, new String[] { "Eb3", "G3",
				"Bb3", "D4" });

		final Chord eMaj7 = new Chord(Pitch.fromString("E3"), Type.MAJOR_7TH);
		assertChordContainsPitches(eMaj7, new String[] { "E3", "G#3", "B3",
				"D#4" });

		final Chord fMaj7 = new Chord(Pitch.fromString("F3"), Type.MAJOR_7TH);
		assertChordContainsPitches(fMaj7,
				new String[] { "F3", "A3", "C4", "E4" });

		final Chord fSharpMaj7 = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR_7TH);
		assertChordContainsPitches(fSharpMaj7, new String[] { "F#3", "A#3",
				"C#4", "E#4" });

		final Chord gMaj7 = new Chord(Pitch.fromString("G3"), Type.MAJOR_7TH);
		assertChordContainsPitches(gMaj7, new String[] { "G3", "B3", "D4",
				"F#4" });

		final Chord aFlatMaj7 = new Chord(Pitch.fromString("Ab3"),
				Type.MAJOR_7TH);
		assertChordContainsPitches(aFlatMaj7, new String[] { "Ab3", "C4",
				"Eb4", "G4" });

		final Chord aMaj7 = new Chord(Pitch.fromString("A3"), Type.MAJOR_7TH);
		assertChordContainsPitches(aMaj7, new String[] { "A3", "C#4", "E4",
				"G#4" });

		final Chord bFlatMaj7 = new Chord(Pitch.fromString("Bb3"),
				Type.MAJOR_7TH);
		assertChordContainsPitches(bFlatMaj7, new String[] { "Bb3", "D4", "F4",
				"A4" });

		final Chord bMaj7 = new Chord(Pitch.fromString("B3"), Type.MAJOR_7TH);
		assertChordContainsPitches(bMaj7, new String[] { "B3", "D#4", "F#4",
				"A#4" });
	}

	@Test
	public void testMinorSeventhChordPitches() {
		final Chord cMin7 = new Chord(Pitch.fromString("C3"), Type.MINOR_7TH);
		assertChordContainsPitches(cMin7, new String[] { "C3", "Eb3", "G3",
				"Bb3" });

		final Chord cSharpMin7 = new Chord(Pitch.fromString("C#3"),
				Type.MINOR_7TH);
		assertChordContainsPitches(cSharpMin7, new String[] { "C#3", "E3",
				"G#3", "B3" });

		final Chord dMin7 = new Chord(Pitch.fromString("D3"), Type.MINOR_7TH);
		assertChordContainsPitches(dMin7,
				new String[] { "D3", "F3", "A3", "C4" });

		final Chord eFlatMin7 = new Chord(Pitch.fromString("Eb3"),
				Type.MINOR_7TH);
		assertChordContainsPitches(eFlatMin7, new String[] { "Eb3", "Gb3",
				"Bb3", "Db4" });

		final Chord eMin7 = new Chord(Pitch.fromString("E3"), Type.MINOR_7TH);
		assertChordContainsPitches(eMin7,
				new String[] { "E3", "G3", "B3", "D4" });

		final Chord fMin7 = new Chord(Pitch.fromString("F3"), Type.MINOR_7TH);
		assertChordContainsPitches(fMin7, new String[] { "F3", "Ab3", "C4",
				"Eb4" });

		final Chord fSharpMin7 = new Chord(Pitch.fromString("F#3"),
				Type.MINOR_7TH);
		assertChordContainsPitches(fSharpMin7, new String[] { "F#3", "A3",
				"C#4", "E4" });

		final Chord gMin7 = new Chord(Pitch.fromString("G3"), Type.MINOR_7TH);
		assertChordContainsPitches(gMin7, new String[] { "G3", "Bb3", "D4",
				"F4" });

		final Chord aFlatMin7 = new Chord(Pitch.fromString("Ab3"),
				Type.MINOR_7TH);
		assertChordContainsPitches(aFlatMin7, new String[] { "Ab3", "Cb4",
				"Eb4", "Gb4" });

		final Chord aMin7 = new Chord(Pitch.fromString("A3"), Type.MINOR_7TH);
		assertChordContainsPitches(aMin7,
				new String[] { "A3", "C4", "E4", "G4" });

		final Chord bFlatMin7 = new Chord(Pitch.fromString("Bb3"),
				Type.MINOR_7TH);
		assertChordContainsPitches(bFlatMin7, new String[] { "Bb3", "Db4",
				"F4", "Ab4" });

		final Chord bMin7 = new Chord(Pitch.fromString("B3"), Type.MINOR_7TH);
		assertChordContainsPitches(bMin7, new String[] { "B3", "D4", "F#4",
				"A4" });
	}

	@Test
	public void testMajorInversion() {
		final Chord cMajFirstInv = new Chord(Pitch.fromString("C3"),
				Type.MAJOR, 1);
		assertChordContainsPitches(cMajFirstInv, new String[] { "C4", "E3",
				"G3" });

		final Chord cMajSecondInv = new Chord(Pitch.fromString("C3"),
				Type.MAJOR, 2);
		assertChordContainsPitches(cMajSecondInv, new String[] { "C4", "E4",
				"G3" });

		final Chord fSharpMajFirstInv = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR, 1);
		assertChordContainsPitches(fSharpMajFirstInv, new String[] { "F#4",
				"A#3", "C#4" });

		final Chord fSharpMajSecondInv = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR, 2);
		assertChordContainsPitches(fSharpMajSecondInv, new String[] { "F#4",
				"A#4", "C#4" });
	}

	@Test
	public void testMinorInversion() {
		final Chord cMinFirstInv = new Chord(Pitch.fromString("C3"),
				Type.MINOR, 1);
		assertChordContainsPitches(cMinFirstInv, new String[] { "C4", "Eb3",
				"G3" });

		final Chord cMinSecondInv = new Chord(Pitch.fromString("C3"),
				Type.MINOR, 2);
		assertChordContainsPitches(cMinSecondInv, new String[] { "C4", "Eb4",
				"G3" });

		final Chord fSharpMinFirstInv = new Chord(Pitch.fromString("F#3"),
				Type.MINOR, 1);
		assertChordContainsPitches(fSharpMinFirstInv, new String[] { "F#4",
				"A3", "C#4" });

		final Chord fSharpMinSecondInv = new Chord(Pitch.fromString("F#3"),
				Type.MINOR, 2);
		assertChordContainsPitches(fSharpMinSecondInv, new String[] { "F#4",
				"A4", "C#4" });
	}

	@Test
	public void testAugmentedInversion() {
		final Chord cAugFirstInv = new Chord(Pitch.fromString("C3"),
				Type.AUGMENTED, 1);
		assertChordContainsPitches(cAugFirstInv, new String[] { "C4", "E3",
				"G#3" });

		final Chord cAugSecondInv = new Chord(Pitch.fromString("C3"),
				Type.AUGMENTED, 2);
		assertChordContainsPitches(cAugSecondInv, new String[] { "C4", "E4",
				"G#3" });

		final Chord fSharpAugFirstInv = new Chord(Pitch.fromString("F#3"),
				Type.AUGMENTED, 1);
		assertChordContainsPitches(fSharpAugFirstInv, new String[] { "F#4",
				"A#3", "D4" });

		final Chord fSharpAugSecondInv = new Chord(Pitch.fromString("F#3"),
				Type.AUGMENTED, 2);
		assertChordContainsPitches(fSharpAugSecondInv, new String[] { "F#4",
				"A#4", "D4" });
	}

	@Test
	public void testDiminishedInversion() {
		final Chord cDimFirstInv = new Chord(Pitch.fromString("C3"),
				Type.DIMINISHED, 1);
		assertChordContainsPitches(cDimFirstInv, new String[] { "C4", "Eb3",
				"Gb3" });

		final Chord cDimSecondInv = new Chord(Pitch.fromString("C3"),
				Type.DIMINISHED, 2);
		assertChordContainsPitches(cDimSecondInv, new String[] { "C4", "Eb4",
				"Gb3" });

		final Chord fSharpDimFirstInv = new Chord(Pitch.fromString("F#3"),
				Type.DIMINISHED, 1);
		assertChordContainsPitches(fSharpDimFirstInv, new String[] { "F#4",
				"A3", "C4" });

		final Chord fSharpDimSecondInv = new Chord(Pitch.fromString("F#3"),
				Type.DIMINISHED, 2);
		assertChordContainsPitches(fSharpDimSecondInv, new String[] { "F#4",
				"A4", "C4" });
	}

	@Test
	public void testDominantSeventhInversion() {
		final Chord cDom7FirstInv = new Chord(Pitch.fromString("C3"),
				Type.DOMINANT_7TH, 1);
		assertChordContainsPitches(cDom7FirstInv, new String[] { "C4", "E3",
				"G3", "Bb3" });

		final Chord cDom7SecondInv = new Chord(Pitch.fromString("C3"),
				Type.DOMINANT_7TH, 2);
		assertChordContainsPitches(cDom7SecondInv, new String[] { "C4", "E4",
				"G3", "Bb3" });

		final Chord cDom7ThirdInv = new Chord(Pitch.fromString("C3"),
				Type.DOMINANT_7TH, 3);
		assertChordContainsPitches(cDom7ThirdInv, new String[] { "C4", "E4",
				"G4", "Bb3" });

		final Chord fSharpDom7FirstInv = new Chord(Pitch.fromString("F#3"),
				Type.DOMINANT_7TH, 1);
		assertChordContainsPitches(fSharpDom7FirstInv, new String[] { "F#4",
				"A#3", "C#4", "E4" });

		final Chord fSharpDom7SecondInv = new Chord(Pitch.fromString("F#3"),
				Type.DOMINANT_7TH, 2);
		assertChordContainsPitches(fSharpDom7SecondInv, new String[] { "F#4",
				"A#4", "C#4", "E4" });

		final Chord fSharpDom7ThirdInv = new Chord(Pitch.fromString("F#3"),
				Type.DOMINANT_7TH, 3);
		assertChordContainsPitches(fSharpDom7ThirdInv, new String[] { "F#4",
				"A#4", "C#5", "E4" });
	}

	@Test
	public void testMajorSeventhInversion() {
		final Chord cMaj7FirstInv = new Chord(Pitch.fromString("C3"),
				Type.MAJOR_7TH, 1);
		assertChordContainsPitches(cMaj7FirstInv, new String[] { "C4", "E3",
				"G3", "B3" });

		final Chord cMaj7SecondInv = new Chord(Pitch.fromString("C3"),
				Type.MAJOR_7TH, 2);
		assertChordContainsPitches(cMaj7SecondInv, new String[] { "C4", "E4",
				"G3", "B3" });

		final Chord cMaj7ThirdInv = new Chord(Pitch.fromString("C3"),
				Type.MAJOR_7TH, 3);
		assertChordContainsPitches(cMaj7ThirdInv, new String[] { "C4", "E4",
				"G4", "B3" });

		final Chord fSharpMaj7FirstInv = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR_7TH, 1);
		assertChordContainsPitches(fSharpMaj7FirstInv, new String[] { "F#4",
				"A#3", "C#4", "E#4" });

		final Chord fSharpMaj7SecondInv = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR_7TH, 2);
		assertChordContainsPitches(fSharpMaj7SecondInv, new String[] { "F#4",
				"A#4", "C#4", "E#4" });

		final Chord fSharpMaj7ThirdInv = new Chord(Pitch.fromString("F#3"),
				Type.MAJOR_7TH, 3);
		assertChordContainsPitches(fSharpMaj7ThirdInv, new String[] { "F#4",
				"A#4", "C#5", "E#4" });
	}

	@Test
	public void testMinorSeventhInversion() {
		final Chord cMin7FirstInv = new Chord(Pitch.fromString("C3"),
				Type.MINOR_7TH, 1);
		assertChordContainsPitches(cMin7FirstInv, new String[] { "C4", "Eb3",
				"G3", "Bb3" });

		final Chord cMin7SecondInv = new Chord(Pitch.fromString("C3"),
				Type.MINOR_7TH, 2);
		assertChordContainsPitches(cMin7SecondInv, new String[] { "C4", "Eb4",
				"G3", "Bb3" });

		final Chord cMin7ThirdInv = new Chord(Pitch.fromString("C3"),
				Type.MINOR_7TH, 3);
		assertChordContainsPitches(cMin7ThirdInv, new String[] { "C4", "Eb4",
				"G4", "Bb3" });

		final Chord fSharpMin7FirstInv = new Chord(Pitch.fromString("F#3"),
				Type.MINOR_7TH, 1);
		assertChordContainsPitches(fSharpMin7FirstInv, new String[] { "F#4",
				"A3", "C#4", "E4" });

		final Chord fSharpMin7SecondInv = new Chord(Pitch.fromString("F#3"),
				Type.MINOR_7TH, 2);
		assertChordContainsPitches(fSharpMin7SecondInv, new String[] { "F#4",
				"A4", "C#4", "E4" });

		final Chord fSharpMin7ThirdInv = new Chord(Pitch.fromString("F#3"),
				Type.MINOR_7TH, 3);
		assertChordContainsPitches(fSharpMin7ThirdInv, new String[] { "F#4",
				"A4", "C#5", "E4" });
	}

	private static void assertChordContainsPitches(final @NonNull Chord chord,
			final String @NonNull [] pitches) {
		final Pitch[] expectedPitches = new Pitch[pitches.length];
		for (int i = 0; i < pitches.length; i++) {
			expectedPitches[i] = Pitch.fromString(pitches[i]);
		}
		final Pitch[] actualPitches = chord.getPitches();

		for (final Pitch pitch : expectedPitches) {
			// Ist pitch Element von actualPitches?
			// Es wird dabei kein Unterschied zwischen enharmonisch
			// verwechselten Tönen gemacht, etwa E# und F.
			final boolean contains = Arrays
					.stream(actualPitches)
					.filter(p -> EqualTemperament.areEnharmonicTogether(p,
							pitch)).findFirst().isPresent();
			if (!contains) {
				final String pitchStr = formatPitchesFromChord(chord);
				fail("Expected " + pitch.toEnglishNotation() + " is in chord "
						+ chord.toString() + " (containing pitches are: "
						+ pitchStr + ")");
			}
		}
	}

	private static String formatPitchesFromChord(final @NonNull Chord chord) {
		final StringBuilder sb = new StringBuilder();
		final Pitch[] pitches = chord.getPitches();
		for (int i = 0; i < pitches.length; i++) {
			final Pitch pitch = pitches[i];
			final boolean last = i == (pitches.length - 1);
			sb.append(pitch.toEnglishNotation());
			if (!last) {
				sb.append(", ");
			}
		}

		return sb.toString();
	}
}

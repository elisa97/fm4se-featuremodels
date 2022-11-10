package de.buw.fm4se.featuremodels;

import java.util.ArrayList;
import java.util.List;

import de.buw.fm4se.featuremodels.exec.LimbooleExecutor;
import de.buw.fm4se.featuremodels.fm.FeatureModel;
import de.buw.fm4se.featuremodels.fm.Feature;

/**
 * This code needs to be implemented by translating FMs to input for Limboole
 * and interpreting the output
 *
 */
public class FeatureModelAnalyzer {

  public static boolean checkConsistent(FeatureModel fm) {
    String formula = FeatureModelTranslator.translateToFormula(fm);
    
    String result;
    try {
      result = LimbooleExecutor.runLimboole(formula, true);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    if (result.contains("UNSATISFIABLE")) {
      return false;
    }
    return true;
  }

  public static List<String> deadFeatureNames(FeatureModel fm) {
    List<String> deadFeatures = new ArrayList<>();
    List<String> listFeatures = new ArrayList<>();
    listFeatures = getFeatures(fm.getRoot(), listFeatures);

    String formula = FeatureModelTranslator.translateToFormula(fm);
    String result = "";

    for (String f : listFeatures) {
      try {
        result = LimbooleExecutor.runLimboole(formula + " & " + f, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (result.contains("UNSATISFIABLE")) {
        deadFeatures.add(f);
      }
    }
      
    return deadFeatures;
  }

  public static List<String> mandatoryFeatureNames(FeatureModel fm) {
    List<String> mandatoryFeatures = new ArrayList<>();
    List<String> listFeatures = new ArrayList<>();
    listFeatures = getFeatures(fm.getRoot(), listFeatures);

    String formula = FeatureModelTranslator.translateToFormula(fm);
    String result = "";

    for (String f : listFeatures) {
      try {
        result = LimbooleExecutor.runLimboole(formula + " & !" + f, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (result.contains("UNSATISFIABLE")) {
        mandatoryFeatures.add(f);
      }
    }

    return mandatoryFeatures;
  }

  private static List<String> getFeatures(Feature f, List<String>l) {
    String parent = f.getName();
    l.add(parent);
    for (Feature child : f.getChildren()) {
      l = getFeatures(child, l);
    }
    return l;
  }
}

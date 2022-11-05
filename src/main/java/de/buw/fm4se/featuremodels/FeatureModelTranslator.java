package de.buw.fm4se.featuremodels;

import java.util.ArrayList;
import java.util.List;

import de.buw.fm4se.featuremodels.fm.FeatureModel;
import de.buw.fm4se.featuremodels.fm.Feature;
import de.buw.fm4se.featuremodels.fm.GroupKind;
import de.buw.fm4se.featuremodels.fm.CrossTreeConstraint;

public class FeatureModelTranslator {
  public static String translateToFormula(FeatureModel fm) {

    // TODO implement a real translation
    Feature f = fm.getRoot();

    List<String> queue = new ArrayList<>();
    queue = addFeature(f, queue);

    // get root name
    String s = f.getName();

    // check constraint
    String constraint = addConstraint(fm.getConstraints());

    // wrap all rules in bracket and append
    for (String q : queue) {
      s += " & " + wrapInBracket(q);
    }

    // wrap all constraints in bracket and append
    s += " & " + wrapInBracket(constraint);
    
    return s;
  }
  
  private static List<String> addFeature(Feature f, List<String> queue) {
    // queue.add(f.getName());
    String parent = f.getName();
    List<String> children = new ArrayList<>();

    for (Feature child : f.getChildren()) {
      children.add(child.getName());

      // parent is a parent of child
      queue.add(child.getName() + " -> " + parent);

      // child is a mandatory subfeature of child
      if (child.isMandatory()) {
        queue.add(parent + " -> " + child.getName());
      }
      queue = addFeature(child, queue);
    }

    if (children.size() > 0 & !GroupKind.NONE.equals(f.getChildGroupKind())) {
      // parent has children in an OR-group
      if (GroupKind.OR.equals(f.getChildGroupKind())) {
        queue.add(parent + " -> " + groupKindTranslate("OR", children));
      } else {
        queue.add(parent + " -> " + groupKindTranslate("XOR", children));
      }
    }

    return queue;
  }
  
  private static String groupKindTranslate(String type, List<String> children) {
    String s = "";
    String c1 = children.get(0);
    String c2 = children.get(1);

    if (type == "OR") {
      // s += wrapInBracket(c1 + " | " + c2);
      for (String c : children) {
        s += c + " | ";
      }
      // remove the last |
      s = s.substring(0, s.length() - 3);
    } else {
      s += wrapInBracket(xorPermGenerator(children));
    }

    // s = s.substring(0, s.length() - 2);  
    // System.out.println(s);
    
    return s;
  }
  
  private static String addConstraint(List<CrossTreeConstraint> constraints) {
    List<String> listConstraints = new ArrayList<>();
    String s = "";

    for (CrossTreeConstraint ctc : constraints) {
      String left = ctc.getLeft().getName();
      String right = ctc.getRight().getName();

      // check type of constraint 
      if (ctc.getKind() == CrossTreeConstraint.Kind.REQUIRES) {
        listConstraints.add(left + " -> " + right);
      } else {
        listConstraints.add("!" + left + "| !" + right);
      }
    }
    
    // join with &
    for (String co : listConstraints) {
      s += co + " & ";
    }

    // remove the last &
    s = s.substring(0, s.length() - 3);  
    return s;
  }

  private static String wrapInBracket(String s) {
    return "(" + s + ")";
  }

  private static String xorPermGenerator(List<String> children) {
    int n = (int) Math.pow(2, children.size());
    List<String> oddCombi = new ArrayList<>();
    List<List<String>> newChildren = new ArrayList<>();
    String s = "";

    for (int i = 0; i < n; i++) {
      String binaryNums = String.format("%" + children.size() + "s", Integer.toBinaryString(i)).replace(' ', '0');
      int counter = 0;
      for (char num : binaryNums.toCharArray()) {
        if (num == '1') {
          counter++;
        }
      }
      if (counter % 2 != 0) {
        oddCombi.add(binaryNums);
      }
    }
    for (String o : oddCombi) {
      List<String> childCombi = new ArrayList<>(children);
      for (int i = 0; i < children.size(); i++) {
        if (o.charAt(i) == '1') {
          childCombi.set(i, "!" + children.get(i));
        }
      }
      newChildren.add(childCombi);
    }

    for (List<String> combo: newChildren) {
      for (String c : combo) {
        s += c + " & ";
      }
      s = s.substring(0, s.length() - 3); 
      s += " | ";
    }
    s = s.substring(0, s.length() - 3); 

    return s;
  }
}

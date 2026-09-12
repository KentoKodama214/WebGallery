package com.web.gallery.architecture;

import static org.junit.jupiter.api.Assertions.fail;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * インターフェースと実装クラスが1対1で対応していることを検証するユーティリティ
 *
 * <p>ArchUnitのフルーエントAPI（{@code classes().that()...should()...}）は単一クラスの性質しか検証できないため、
 * 「インターフェースに対応する実装クラスが存在するか」「その逆」というインポート済みクラス全体を横断する検証は、{@link JavaClasses}
 * を直接受け取る{@code @ArchTest}メソッド形式で実装する。
 */
final class InterfaceImplPairing {

  /**
   * インターフェースと実装クラスの1対1対応を検証する
   *
   * @param classes インポート済みの全クラス
   * @param interfacePackage インターフェースが配置されるパッケージ名
   * @param implPackage 実装クラスが配置されるパッケージ名
   * @param implSuffix 実装クラス名のサフィックス（例: "Impl"）
   */
  static void verify(
      JavaClasses classes, String interfacePackage, String implPackage, String implSuffix) {
    Map<String, JavaClass> interfaces = topLevelClassesByName(classes, interfacePackage, true);
    Map<String, JavaClass> impls = topLevelClassesByName(classes, implPackage, false);
    List<String> violations = new ArrayList<>();

    for (Map.Entry<String, JavaClass> entry : interfaces.entrySet()) {
      String implName = entry.getKey() + implSuffix;
      JavaClass impl = impls.get(implName);
      if (impl == null) {
        violations.add(
            String.format(
                "インターフェース%s(%s)に対応する実装クラス%s(%s)が存在しません",
                entry.getKey(), interfacePackage, implName, implPackage));
      } else if (!impl.getAllRawInterfaces().contains(entry.getValue())) {
        violations.add(String.format("%sは%sを実装していません", implName, entry.getKey()));
      }
    }

    for (Map.Entry<String, JavaClass> entry : impls.entrySet()) {
      String name = entry.getKey();
      String interfaceName =
          name.endsWith(implSuffix) ? name.substring(0, name.length() - implSuffix.length()) : "";
      if (interfaceName.isEmpty() || !interfaces.containsKey(interfaceName)) {
        violations.add(
            String.format(
                "実装クラス%s(%s)に対応するインターフェースが%sに存在しません", name, implPackage, interfacePackage));
      }
    }

    if (!violations.isEmpty()) {
      fail(String.join(System.lineSeparator(), violations));
    }
  }

  private static Map<String, JavaClass> topLevelClassesByName(
      JavaClasses classes, String packageName, boolean interfacesOnly) {
    Map<String, JavaClass> result = new HashMap<>();
    for (JavaClass javaClass : classes) {
      if (!javaClass.getPackageName().equals(packageName)) {
        continue;
      }
      if (javaClass.getEnclosingClass().isPresent()) {
        continue;
      }
      if (javaClass.isInterface() != interfacesOnly) {
        continue;
      }
      result.put(javaClass.getSimpleName(), javaClass);
    }
    return result;
  }

  private InterfaceImplPairing() {}
}

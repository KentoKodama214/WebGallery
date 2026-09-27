---
paths:
  - backend/**/exception/**
---

# Exceptionクラスのアーキテクチャルール

`domain/exception/`に配置する。既知の設計負債として、`domain/enumeration/ErrorEnum#toException()`が`domain/exception/`のクラスを参照し、`GalleryException`が`ErrorEnum`を参照する相互依存が存在する。パッケージレベルの循環ではあるが、両者とも`domain/`内部に閉じており、オニオンアーキテクチャの層境界（`domain`が外側の層に依存しないこと）は破っていないため、現状は許容している。

## 命名規則

- クラス名サフィックス: `Exception`

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/NamingSuffixArchitectureTest.java`のArchUnitテストで機械的に検証される。

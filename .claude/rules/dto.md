---
paths:
  - backend/**/dto/**
---

# DTOクラスのアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Dto`

## Lombokアノテーション規約

- **許可**: `@Data` のみ
- **禁止**: `@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@Value`

## プロパティ

- ドメインクラス（値オブジェクト）、Modelクラス、Entityクラスは使用しない（依存性・責務の分離のため）

## 検証

クラス名サフィックスとプロパティの型制約は`backend/src/test/java/com/web/gallery/architecture/DtoArchitectureTest.java`のArchUnitテストで機械的に検証される。ただし`@Data`のみというLombokアノテーション規約は、コンパイラが`RetentionPolicy.SOURCE`で完全に除去しバイトコードに一切残らないため、バイトコード解析であるArchUnitでは原理的に検証不可能であり、`backend-architecture-checker`によるレビューで担保する。

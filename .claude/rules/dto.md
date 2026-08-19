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

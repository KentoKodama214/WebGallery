---
paths:
  - backend/**/entity/**
---

# Entityクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Data` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Value`、`@Getter`、`@Setter`

## プロパティ

- ドメインクラス（値オブジェクト）、Modelクラス、Dtoクラスは使用しない（依存性・責務の分離のため）

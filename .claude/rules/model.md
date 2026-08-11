---
paths:
  - backend/**/model/**
---

# Modelクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Value` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Data`、`@Getter`、`@Setter`

## @NonNullアノテーション

- Null許容しないプロパティには`@NonNull`アノテーションを付与すること
- `@NonNull`が一つも使われていないModelクラスは違反の可能性がある

## 命名規則

- クラス名サフィックス: `Model`

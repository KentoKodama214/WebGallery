---
paths:
  - backend/**/dto/**
---

# DTOクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Data` のみ
- **禁止**: `@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@Value`

## 命名規則

- クラス名サフィックス: `Dto`

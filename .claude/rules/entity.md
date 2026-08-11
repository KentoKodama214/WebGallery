---
paths:
  - backend/**/entity/**
---

# Entityクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Data` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Value`、`@Getter`、`@Setter`

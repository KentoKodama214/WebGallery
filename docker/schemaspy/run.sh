#!/bin/bash
set -e

echo "=== commonスキーマのドキュメント生成 ==="
java -jar /usr/local/lib/schemaspy/schemaspy.jar -configFile /config/schemaspy-common.properties

echo "=== photoスキーマのドキュメント生成 ==="
java -jar /usr/local/lib/schemaspy/schemaspy.jar -configFile /config/schemaspy-photo.properties

echo "=== ドキュメント生成完了 ==="

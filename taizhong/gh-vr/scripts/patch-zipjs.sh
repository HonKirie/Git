#!/bin/bash
# 修复 @zip.js/zip.js v2.x 目录结构变更导致 Cesium 引用旧路径报错
# Cesium @1.127 硬编码 import "@zip.js/zip.js/lib/zip-no-worker.js"
# 但 @zip.js/zip.js@2.14.0 已重构目录，该文件不存在
ZIP_DIR=$(find node_modules/.pnpm -path "*/@zip.js+zip.js@*/node_modules/@zip.js/zip.js" -type d | head -1)
if [ -n "$ZIP_DIR" ] && [ ! -f "$ZIP_DIR/lib/zip-no-worker.js" ]; then
  mkdir -p "$ZIP_DIR/lib"
  cp "$ZIP_DIR/index.js" "$ZIP_DIR/lib/zip-no-worker.js"
  echo "[patch] created $ZIP_DIR/lib/zip-no-worker.js"
fi

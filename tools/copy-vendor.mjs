import { copyFileSync, mkdirSync, rmSync } from "node:fs";
import { dirname, join } from "node:path";

const root = process.cwd();
const nodeModules = join(root, "node_modules");
const vendorRoot = join(root, "src", "main", "resources", "static", "vendor");

function ensureParentDir(filePath) {
  mkdirSync(dirname(filePath), { recursive: true });
}

function copy(sourceRelativePath, targetRelativePath) {
  const sourcePath = join(nodeModules, sourceRelativePath);
  const targetPath = join(vendorRoot, targetRelativePath);
  ensureParentDir(targetPath);
  copyFileSync(sourcePath, targetPath);
  console.log(`Copied ${sourceRelativePath} -> ${targetRelativePath}`);
}

rmSync(vendorRoot, { recursive: true, force: true });

copy("bootstrap/dist/css/bootstrap.min.css", "bootstrap/css/bootstrap.min.css");
copy("dropzone/dist/dropzone.css", "dropzone/dropzone.css");
copy("dropzone/dist/dropzone.js", "dropzone/dropzone.js");
copy("@highlightjs/cdn-assets/styles/dark.min.css", "highlightjs/styles/dark.min.css");
copy("@highlightjs/cdn-assets/highlight.min.js", "highlightjs/highlight.min.js");

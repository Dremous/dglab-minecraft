# Gradle Package 工作流使用说明

## 功能

自动在 GitHub 上创建 Release 时构建 mod 并发布到 GitHub Packages。

## 使用步骤

### 1. 预配置

已配置好 `build.gradle` 和 `gradle-publish.yml`。

### 2. 创建 GitHub Release

```bash
# 方法1: 使用 GitHub 网页
# 1. 访问: https://github.com/Dremous/dglab-minecraft/releases
# 2. 点击 "New release"
# 3. 输入版本号（与 gradle.properties 中的 mod_version 一致，如 1.0.0）
# 4. 上传构建好的 jar 文件
# 5. 点击 "Publish release"

# 方法2: 使用 git 命令行
git tag v1.0.0
git push origin v1.0.0
# 然后在 GitHub 网页上编辑这个 tag 创建 release
```

### 3. 自动构建

工作流触发后会自动：
- 构建 mod jar 文件
- 创建源代码 jar 文件
- 发布到 GitHub Packages: `https://github.com/Dremous/dglab-minecraft/packages`

### 4. 下载发布

- 访问 GitHub Packages: https://github.com/Dremous/dglab-minecraft/packages
- 选择 `dglab-minecraft` 包
- 查看版本历史下载 jar 文件

## 注意事项

- 版本号必须与 `gradle.properties` 中的 `mod_version` 一致
- 创建 Release 时会自动运行构建
- 需要授予 workflow 权限（已在配置中设置）

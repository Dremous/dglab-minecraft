# DG-LAB Minecraft Mod 文档

## 项目概述

DG-LAB Minecraft Mod 是一款将 Minecraft 游戏与 DG-LAB 电刺激设备集成的 Fabric mod。当玩家在游戏中受到伤害时，mod 会向 DG-LAB 设备发送相应的电脉冲，提供沉浸式的游戏体验。

### 功能特点

- **伤害触发电脉冲**: 玩家受伤时自动发送电脉冲
- **伤害类型识别**: 不同伤害类型对应不同的电脉冲波形
- **双连接模式**: 支持本地服务器和远程 WebSocket 两种连接方式
- **GUI 配置界面**: 按 `J` 键打开设置界面
- **命令行命令**: 提供 `/dglab` 系列命令进行控制
- **二维码连接**: 生成二维码供 DG-LAB APP 扫描连接

---

## 技术架构

### 项目结构

```
src/main/java/com/dglab/minecraft/
├── DGLabMinecraft.java          # 主入口类
├── DGLabConfig.java             # 配置类
├── DGLabLocalServer.java        # 本地 WebSocket 服务器
├── DGLabWebSocketClient.java    # 远程 WebSocket 客户端
├── NetworkAdapter.java          # 网络适配器
├── DamageListener.java          # 伤害监听器
├── DamageType.java              # 伤害类型枚举
├── WaveType.java                # 波形类型枚举
├── PulseGenerator.java          # 脉冲生成器
├── DGLabCommands.java           # 命令注册
└── gui/
    ├── DGLabScreen.java         # GUI 界面
    └── QRCodeGenerator.java     # 二维码生成
```

### 技术栈

- **Minecraft 版本**: 1.20.4+
- **Mod Loader**: Fabric
- **依赖库**:
  - Java-WebSocket 1.5.6 (WebSocket 通信)
  - Google Gson 2.10.1 (JSON 解析)
  - ZXing Core 3.5.2 (二维码生成)

---

## 核心模块详解

### 1. 伤害检测机制

使用 Mixin 技术拦截 `PlayerEntity.damage()` 方法:

```java
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && amount > 0) {
            DamageType damageType = DamageType.fromDamageSource(source.getName());
            DGLabMinecraft.setLastDamage(amount, damageType);
        }
    }
}
```

### 2. 伤害类型系统

`DamageType` 枚举定义了 20+ 种伤害类型，每种类型包含:

| 属性 | 说明 |
|------|------|
| `baseStrength` | 基础强度 |
| `strengthMultiplier` | 强度乘数 |
| `durationMultiplier` | 持续时间乘数 |
| `waveType` | 对应的波形类型 |

**支持的伤害类型**:

| 伤害类型 | 基础强度 | 波形 | 说明 |
|----------|----------|------|------|
| GENERIC | 50 | STABLE | 通用伤害 |
| FIRE | 80 | BURNING | 火伤害 |
| LAVA | 100 | BURNING | 熔岩伤害 |
| FALL | 60 | IMPACT | 摔落伤害 |
| DROWN | 30 | GRADUAL | 溺水伤害 |
| EXPLOSION | 100 | IMPACT | 爆炸伤害 |
| LIGHTNING | 100 | ELECTRIC | 闪电伤害 |
| MAGIC | 90 | ELECTRIC | 魔法伤害 |
| MOB_ATTACK | 70 | IMPACT | 生物攻击 |
| PLAYER_ATTACK | 80 | IMPACT | 玩家攻击 |
| ... | ... | ... | ... |

### 3. 波形类型系统

`WaveType` 枚举定义了 8 种电脉冲波形:

| 波形类型 | 特征 | 适用场景 |
|----------|------|----------|
| STABLE | 恒定频率和强度 | 通用伤害 |
| IMPACT | 高频开场，逐渐减弱 | 摔落、爆炸、攻击 |
| BURNING | 周期性变化 | 火、熔岩伤害 |
| SHARP | 强度突变，尖峰 | 尖锐物体伤害 |
| ELECTRIC | 随机变化 | 闪电、魔法伤害 |
| FLUCTUATE | 正弦波动 | Wither 效果 |
| GRADUAL | 逐渐增强 | 溺水、饥饿 |
| PULSE | 交替强弱 | 脉冲效果 |

### 4. 连接模式

#### 本地服务器模式

- 启动本地 WebSocket 服务器 (默认端口 9999)
- 通过局域网 IP 连接 DG-LAB APP
- 生成二维码供 APP 扫描连接
- 适合局域网使用，延迟更低

#### 远程服务器模式

- 连接到远程 WebSocket 服务器
- 默认服务器: `wss://ws.dungeon-lab.cn`
- 适合外网连接使用

### 5. 脉冲数据格式

使用 B0 协议生成脉冲数据:

```
B0 00 [mode] [A_strength] [B_strength] [f1][f2][f3][f4] [s1][s2][s3][s4] ...
```

---

## 配置参数

### DGLabConfig

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `channel` | 1 | 输出通道 (1=A, 2=B) |
| `baseStrength` | 20 | 基础强度 (1-100) |
| `maxStrength` | 80 | 最大强度 (1-100) |
| `baseDuration` | 100 | 基础持续时间 (ms) |
| `maxDuration` | 500 | 最大持续时间 (ms) |
| `baseFrequency` | 50 | 基础频率 |
| `enabled` | true | 是否启用 |
| `damageMultiplier` | 1.0 | 伤害乘数 |
| `useLocalServer` | true | 是否使用本地服务器 |
| `serverPort` | 9999 | 本地服务器端口 |
| `autoStartServer` | true | 是否自动启动服务器 |

### 伤害计算公式

```java
strength = min(maxStrength, max(baseStrength, baseStrength + damage * 10 * damageMultiplier))
duration = min(maxDuration, max(baseDuration, baseDuration + damage * 30 * damageMultiplier))
frequency = baseFrequency + min(damage * 5, 50)
```

---

## 使用方法

### GUI 控制

按 `J` 键打开 GUI 配置界面，可设置:
- 连接模式切换
- 服务器启动/停止
- 网络适配器选择
- 通道选择
- 强度滑块
- 测试脉冲按钮

### 命令行命令

| 命令 | 说明 |
|------|------|
| `/dglab connect` | 连接远程服务器 |
| `/dglab disconnect` | 断开连接 |
| `/dglab bind <targetId>` | 绑定到 APP |
| `/dglab enable` | 启用伤害脉冲 |
| `/dglab disable` | 禁用伤害脉冲 |
| `/dglab channel <1|2>` | 设置通道 |
| `/dglab strength <base> <max>` | 设置强度 |
| `/dglab test <strength>` | 发送测试脉冲 |
| `/dglab status` | 查看状态 |
| `/dglab qr` | 显示二维码内容 |

---

## 协议消息格式

### 消息类型

```json
{
  "type": "bind|msg|heartbeat|error",
  "clientId": "string",
  "targetId": "string",
  "message": "string"
}
```

### 脉冲消息

```
pulse-A:[pulse_data_array]
pulse-B:[pulse_data_array]
clear-1
clear-2
```

### 强度消息

```
strength-{channel}+{mode}+{value}
```

---

## 构建与安装

### 开发环境

- Java 21+
- Gradle 8.x
- Minecraft 1.20.4

### 构建命令

```bash
./gradlew build
```

### 安装

将生成的 `.jar` 文件放入 Minecraft 的 `mods` 文件夹中。

---

## 注意事项

1. **仅客户端**: 此 mod 仅为客户端 mod，不需要在服务器上安装
2. **Java 21**: 需要 Java 21 或更高版本
3. **网络要求**: 本地模式需要局域网连接；远程模式需要互联网连接
4. **设备兼容**: 需要 DG-LAB 设备支持 WebSocket 协议

---

## 许可证

MIT License

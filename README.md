# Collision
一个有限状态机系统

## 下载

### Windows
1. 前往 [Releases](https://github.com/gokfzy/collision/releases/latest) 下载最新版本
2. 运行 `Collision.exe`后，会自动安装
3. 系统要求：Windows 10/11，无需额外安装运行时
4. 由于使用jpackage的原因，exe可能会报毒，如果不信任exe，可以通过源代码构建

> 目前仅提供 Windows 版本。
## 简介
本项目围绕主题Collision构造了一个有限状态机系统及其UI界面。
## 系统概述
本系统是一个元胞自动机。每个格子拥有5种状态及一份方向列表
- 三种基本状态
  - 空白：无方向
  - 移动块：恰好拥有一个方向（上、下、左、右之一）
  - 工厂：无方向
- 两种衍生状态
  - 碰撞态：拥有 1 到 4 个不重复方向
  - 处理态：拥有 1 到 4 个不重复方向

总状态数：空白 1 + 移动块 4 + 工厂 1 + 碰撞态 15 + 处理态 15 = 36


### 每次触发更新时
- 不处理空白与工厂
- 移动块向其方向移动一格
- 碰撞态根据方向集合相对于自身位置的指定方向前1格添加一个朝向指定方向的移动块，然后自身变成空白
- 处理态同碰撞态，但在处理后自身变为工厂
- 当多个移动块移动到同一格时，处理规则如下
  - 忽略奇数个移动块碰撞的情况
  - 偶数个移动块碰撞时，在该格形成碰撞态，碰撞态的方向集合处理规则如下
    - 依次取到达方向
    - 若集合中已有其相反方向，则移除该相反方向，并加入两个旋转 90° 后的方向
    - 否则直接加入该方向
- 当多个移动块（包括1个）同时移动到工厂时，处理规则如下
  - 忽略偶数个移动块到达的情况
  - 奇数个移动块到达时，在该格形成处理态，处理态的方向集合处理规则如下
    - 对每个到达方向，若到达方向中存在其相反方向，则跳过该方向
    - 否则加入该方向旋转 +90° 和 -90° 后的两个方向

补充说明
- 全局删除是语义保持的内存优化，不属于 CA 规则

## 使用说明
- 你可以通过点击格子来切换向上的移动块、工厂以及空白
- 你可以通过点击绿色三角更新一次界面
- 点击绿色三角旁的下拉箭头会出现选项，你可以通过点击选项里的“自动更新”进行默认1秒间隔的自动更新，再次点击即可取消
- 点击黑色齿轮以帮助你调整设置
  - 调整“移动方块超出有效区域后的存活步数”能减少不必要的性能消耗，其中“有效范围”指**移动块能对其他非空白格子造成影响的有效范围**；你可以通过把它调得非常大以避免移动块消失，但不建议这么做
  - 调整“更新后执行的步数”能帮助你更快速地进行更新
  - 调整“自动更新的间隔”能使自动更新的间隔调整，以便更好地观察结果
  - 调整“启用追踪”能帮助你获取上次更新时所有非空格子的位置，以便观察运动轨迹
  - 调整“选择语言”能帮助你选择自己的惯用语言
  - 将鼠标移动到设置项按钮旁以查看完整设置项名称，点击它重置设置
  - 通过“应用”保存设置，通过“确定”保存设置并退出，通过“取消”退出但不保存设置

## 构建与运行

### 环境要求

- JDK 27（`pom.xml` 中 `maven.compiler.release=27`，请确保 `java -version` 和 `javac -version` 均为 27）
- Maven 3.8+
- Windows 10/11
- 生成 EXE 安装包需要 JDK 自带的 `jpackage` 工具，并安装 WiX Toolset（jpackage 生成 Windows EXE/MSI 安装包所需）

### 克隆项目

```bash
git clone https://github.com/gokfzy/collision.git
cd collision
```

### 构建

```bash
mvn clean package
```

该命令会完成编译、打包，并将运行时依赖复制到 `target/jpackage-input/lib`，把主 JAR 复制到 `target/jpackage-input`，最后调用 `jpackage` 在 `target/installer` 下生成 Windows 安装包。构建完成后主要产物如下：

- 可执行 JAR：`target/collision-1.0.0.jar`
- jpackage 输入目录：`target/jpackage-input/`
- Windows 安装包：`target/installer/Collision-1.0.0.exe`（具体文件名以实际输出为准）

### 运行

开发时直接启动 JavaFX 应用：

```bash
mvn javafx:run
```

使用打包后的 JAR 运行（需在 `target/jpackage-input` 目录下运行）：

```bash
java -jar target/jpackage-input/collision-1.0.0.jar
```

也可以安装 `target/installer/Collision-1.0.0.exe`，安装后从开始菜单启动 `Collision`。

### 测试

```bash
mvn test
```

## License

本项目基于 [MIT License](LICENSE) 开源。

package com.example.agentpattern.knowledge.loader;

import com.example.agentpattern.knowledge.base.Document;
import com.example.agentpattern.knowledge.base.KnowledgeBase;
import com.example.agentpattern.knowledge.base.KnowledgeBaseRegistry;
import com.example.agentpattern.knowledge.vector.InMemoryVectorKnowledgeBase;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reolink 产品知识库加载器
 * 加载 Reolink 摄像头和安防产品相关的知识库数据
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "knowledge", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReolinkKnowledgeLoader {

    private final KnowledgeBaseRegistry registry;

    public ReolinkKnowledgeLoader(KnowledgeBaseRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void loadReolinkKnowledge() {
        log.info("正在加载 Reolink 产品知识库...");

        // 1. 加载 Reolink 产品手册知识库
        loadReolinkProductManual();

        // 2. 加载 Reolink 技术支持知识库
        loadReolinkTechSupport();

        // 3. 加载 Reolink 安装指南知识库
        loadReolinkInstallationGuide();

        log.info("Reolink 产品知识库加载完成");
    }

    /**
     * 加载 Reolink 产品手册知识库
     */
    private void loadReolinkProductManual() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "reolink-product-manual",
                "Reolink 产品手册和功能说明"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink Argus 4 Pro 产品特性")
                .content("""
                        Reolink Argus 4 Pro 是一款顶级无线安防摄像头，主要特性包括：

                        【画质与视野】
                        - 4K 8MP 双镜头设计，提供超高清画质
                        - 180°超广角视野，几乎无死角监控
                        - 双镜头拼接技术，画面自然流畅

                        【夜视功能】
                        - ColorX 全彩夜视技术，夜晚也能看清颜色
                        - 无需外接补光灯，智能感光自动调节
                        - 夜视距离可达10米

                        【智能检测】
                        - AI 人形检测，减少误报
                        - 智能追踪功能，自动跟踪移动目标
                        - 宠物检测，区分人和动物
                        - 车辆检测，识别车辆进出

                        【供电方式】
                        - 内置可充电电池，续航可达数月
                        - 支持 Reolink 太阳能板供电，永不断电
                        - USB-C 充电接口，充电方便

                        【连接与存储】
                        - 2.4G/5G 双频 WiFi，信号更稳定
                        - 支持 microSD 卡本地存储（最大256GB）
                        - 支持 Reolink NVR 录像
                        - 可选 Reolink Cloud 云存储服务

                        【其他特性】
                        - IP66 防水防尘等级，适合户外使用
                        - 双向语音对讲，支持实时通话
                        - 手机 App 远程查看，支持多人共享
                        - 智能推送通知，异常情况及时提醒
                        """)
                .source("Reolink 产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("PoE 摄像头优势与应用")
                .content("""
                        Reolink PoE 摄像头（如 RLC-810A）采用以太网供电技术，具有以下优势：

                        【什么是 PoE】
                        - PoE = Power over Ethernet（以太网供电）
                        - 一根网线同时传输数据和电力
                        - 符合 IEEE 802.3af/at 标准

                        【PoE 的优势】
                        1. 安装简单：无需单独布线供电，一根网线搞定
                        2. 稳定可靠：有线传输，不受 WiFi 信号干扰
                        3. 传输距离远：单根网线可达100米
                        4. 成本节省：省去电源线和插座成本
                        5. 集中管理：通过 NVR 统一供电和管理

                        【适用场景】
                        - 大型监控系统（多摄像头部署）
                        - 商业场所（店铺、办公室、仓库）
                        - 户外监控（车库、庭院、厂区）
                        - 对稳定性要求高的场合

                        【配套设备】
                        - Reolink NVR 录像机（内置 PoE 交换机）
                        - PoE 交换机（独立供电设备）
                        - 超五类/六类网线（建议使用六类线）

                        【注意事项】
                        - 确认 PoE 供电功率足够（单个摄像头约需12-15W）
                        - 网线长度不超过100米
                        - 使用合格的国标网线
                        """)
                .source("Reolink 产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink 智能门铃功能详解")
                .content("""
                        Reolink Video Doorbell 智能门铃是家庭安防的第一道防线，主要功能包括：

                        【访客监控】
                        - 5MP 超清画质，看清访客面部细节
                        - 180° 超广角，从头到脚全覆盖
                        - 红外夜视，夜晚也能清晰识别

                        【智能检测与通知】
                        - 人形检测，有人靠近立即通知
                        - 预录功能：访客按铃前3秒自动录像
                        - 秒级推送：有人按铃，手机立即收到通知
                        - 可疑徘徊检测，异常行为提醒

                        【双向对讲】
                        - 内置麦克风和扬声器
                        - 手机远程与访客对话
                        - 降噪技术，语音清晰
                        - 不在家也能"开门"迎客

                        【存储方式】
                        - 本地 microSD 卡存储（最大128GB）
                        - 可选 Reolink Cloud 云存储
                        - 循环录像，满了自动覆盖

                        【安装要求】
                        - 需要 8-24V 有线供电（通常使用门铃变压器）
                        - 支持 2.4G/5G 双频 WiFi
                        - 可替换现有有线门铃
                        - 提供安装支架和工具

                        【App 功能】
                        - 实时视频查看
                        - 访客记录回放
                        - 多用户共享
                        - 自定义检测区域
                        - 免打扰时段设置
                        """)
                .source("Reolink 产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink TrackMix 双镜头追踪技术")
                .content("""
                        Reolink TrackMix 采用独特的双镜头追踪技术，实现广角监控与目标追踪的完美结合：

                        【双镜头设计】
                        - 广角镜头：4K 超清，固定监控全景画面
                        - 长焦镜头：4MP 云台，智能追踪移动目标
                        - 双画面同时显示，全景与特写一目了然

                        【智能追踪流程】
                        1. 广角镜头持续监控全景
                        2. AI 检测到移动目标（人/车/宠物）
                        3. 长焦镜头自动转向目标
                        4. 自动变焦拉近，获取清晰特写
                        5. 持续跟踪目标移动轨迹
                        6. 目标消失后，长焦镜头归位

                        【追踪特性】
                        - 355° 水平旋转 + 90° 垂直倾斜
                        - 6倍混合变焦，看清更多细节
                        - 人车宠物分类检测
                        - 优先追踪人形目标
                        - 多目标智能判断

                        【应用场景】
                        - 商店：全景监控 + 顾客行为追踪
                        - 停车场：车位监控 + 车辆进出追踪
                        - 庭院：全景防护 + 异常目标锁定
                        - 仓库：货物监控 + 人员活动追踪

                        【配置建议】
                        - 安装高度：2.5-3.5米最佳
                        - 覆盖范围：单台可监控200平米
                        - 网络要求：建议5G WiFi或有线连接
                        - 存储容量：建议256GB SD卡或NVR
                        """)
                .source("Reolink 产品手册")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded Reolink product manual knowledge base with {} documents", documents.size());
    }

    /**
     * 加载 Reolink 技术支持知识库
     */
    private void loadReolinkTechSupport() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "reolink-tech-support",
                "Reolink 常见技术问题与解决方案"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink 摄像头无法连接 WiFi")
                .content("""
                        Reolink 摄像头 WiFi 连接问题排查与解决：

                        【问题现象】
                        - 添加设备时无法搜索到摄像头
                        - WiFi 配置失败
                        - 摄像头离线，无法远程查看

                        【解决步骤】

                        1. 检查 WiFi 频段
                        - 确认摄像头支持的频段（2.4G/5G）
                        - 部分型号仅支持2.4G，需关闭路由器5G或分开SSID
                        - 检查路由器是否开启双频合一（建议关闭）

                        2. 检查 WiFi 信号强度
                        - 摄像头位置信号强度至少-70dBm以上
                        - 使用手机WiFi分析工具测试信号
                        - 信号弱可添加WiFi中继器或更换路由器位置

                        3. 检查 WiFi 设置
                        - WiFi 密码是否正确（区分大小写）
                        - 路由器安全模式建议使用 WPA2-PSK
                        - 避免使用特殊字符作为WiFi密码
                        - 检查路由器是否开启 MAC 地址过滤

                        4. 重置摄像头
                        - 长按 Reset 按钮10秒重置设备
                        - 听到提示音后松开
                        - 重新配置 WiFi 连接

                        5. 使用有线连接初始化
                        - 部分型号支持先用网线连接
                        - 初始化后再配置WiFi
                        - 配置成功后拔掉网线

                        6. 更新固件
                        - 访问 Reolink 官网下载最新固件
                        - 通过 App 或网页升级固件
                        - 固件更新可能修复连接问题

                        【预防措施】
                        - 定期检查固件更新
                        - 避免频繁更换WiFi密码
                        - 保持路由器固件最新
                        - 使用信誉良好的路由器品牌
                        """)
                .source("Reolink 技术支持")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink 夜视效果不佳或有噪点")
                .content("""
                        Reolink 摄像头夜视问题分析与优化：

                        【常见夜视问题】
                        1. 夜视画面模糊、噪点多
                        2. 夜视距离不足
                        3. 夜视画面发白（过曝）
                        4. 夜视画面全黑

                        【原因分析与解决】

                        1. 环境光照不足
                        - 红外摄像头需要一定环境光
                        - ColorX 型号需要微弱光源（路灯、月光等）
                        - 解决：添加补光灯或选择泛光灯摄像头

                        2. 镜头有污渍或水汽
                        - 灰尘、水汽影响成像质量
                        - 解决：用干布轻轻擦拭镜头
                        - 检查防水胶圈是否老化

                        3. 红外灯反光
                        - 摄像头太靠近墙面或玻璃
                        - 解决：距离墙面至少30cm
                        - 避免透过玻璃拍摄

                        4. 夜视距离超出范围
                        - 不同型号夜视距离不同（10-30米）
                        - 解决：添加补充摄像头覆盖远距离
                        - 或选择夜视距离更远的型号

                        5. 图像设置不当
                        - 亮度、对比度设置过高或过低
                        - 解决：在 App 中调整图像参数
                        - 建议：亮度50，对比度50，饱和度50

                        6. 网络带宽不足
                        - 码率过低导致画质下降
                        - 解决：检查网络速度
                        - 在 App 中调整清晰度为"高清"

                        【优化建议】
                        - ColorX 型号：保持0.01 lux 以上光照
                        - 红外型号：避免正对反光物体
                        - 定期清洁镜头（建议每月一次）
                        - 检查固件更新（可能优化夜视算法）

                        【不同型号夜视特点】
                        - Argus 4 Pro: ColorX 全彩夜视，需微弱光源
                        - RLC-810A: 红外夜视30米，全黑环境可用
                        - Lumus: 内置泛光灯，主动补光全彩夜视
                        - TrackMix: ColorX 夜视，追踪时自动开启聚光灯
                        """)
                .source("Reolink 技术支持")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink NVR 录像机常见问题")
                .content("""
                        Reolink NVR 录像机使用问题排查：

                        【无法检测到摄像头】

                        原因1：网线连接问题
                        - 检查网线是否插紧
                        - 更换网线测试
                        - 确认使用超五类或六类网线
                        - 网线长度不超过100米

                        原因2：PoE 供电不足
                        - 计算总功率是否超出 NVR PoE 供电能力
                        - 8路NVR通常提供120W总功率
                        - 单个4K摄像头约需15W
                        - 超出可添加外置 PoE 交换机

                        原因3：摄像头未激活
                        - 新摄像头需要先激活
                        - 在 NVR 界面设置密码激活
                        - 确保摄像头密码与 NVR 一致

                        【录像无法回放】

                        原因1：硬盘问题
                        - 检查硬盘是否正常识别
                        - 硬盘满了需要格式化或更换
                        - 使用监控级硬盘（如西部数据紫盘）

                        原因2：录像设置问题
                        - 检查录像计划是否开启
                        - 确认录像模式（持续/移动侦测）
                        - 检查存储路径设置

                        原因3：时间不同步
                        - 检查 NVR 系统时间
                        - 开启 NTP 自动对时
                        - 时区设置正确

                        【远程访问无法连接】

                        方法1：使用 Reolink P2P（推荐）
                        - 扫描 NVR 二维码添加设备
                        - 无需公网IP和端口映射
                        - 最简单的远程访问方式

                        方法2：DDNS 动态域名
                        - 在路由器配置 DDNS
                        - 设置端口转发
                        - 使用域名访问 NVR

                        方法3：Reolink Cloud
                        - 订阅云存储服务
                        - 自动建立远程连接
                        - 无需复杂配置

                        【录像存储时间计算】

                        计算公式：
                        存储天数 = 硬盘容量(GB) / (摄像头数量 × 单路码率(Mbps) × 3600 × 24 / 8 / 1024)

                        示例（4路4K摄像头，2TB硬盘）：
                        - 单路码率：8Mbps
                        - 每日存储：8 × 4 × 3600 × 24 / 8 / 1024 ≈ 337GB
                        - 可存储：2048 / 337 ≈ 6天

                        【维护建议】
                        - 每月检查硬盘健康状态
                        - 每季度清理 NVR 内部灰尘
                        - 定期更新 NVR 固件
                        - 备份重要录像到外部存储
                        """)
                .source("Reolink 技术支持")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded Reolink tech support knowledge base with {} documents", documents.size());
    }

    /**
     * 加载 Reolink 安装指南知识库
     */
    private void loadReolinkInstallationGuide() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "reolink-installation-guide",
                "Reolink 产品安装指南与最佳实践"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("户外摄像头安装最佳实践")
                .content("""
                        Reolink 户外摄像头安装指南：

                        【选址原则】

                        1. 高度选择
                        - 推荐安装高度：2.5-3.5米
                        - 太低：容易被破坏，视野受限
                        - 太高：人脸细节不清晰
                        - 门铃高度：1.2-1.5米（齐胸位置）

                        2. 角度设置
                        - 镜头向下倾斜15-30度
                        - 避免正对太阳方向
                        - 避免直射强光源
                        - 考虑监控死角，多机位互补

                        3. 覆盖范围
                        - 单个4K摄像头建议监控15-20米范围
                        - 重要区域建议双机位覆盖
                        - 通道、入口重点监控

                        【网络布线】

                        1. WiFi 摄像头
                        - 确认安装位置WiFi信号强度
                        - 信号弱可增加WiFi中继器
                        - 2.4G穿墙性好，5G速度快
                        - 建议预埋电源线

                        2. PoE 摄像头
                        - 使用超五类或六类网线
                        - 网线最长100米
                        - 户外网线需要防水防晒护套
                        - 预留足够网线长度（建议+2米）

                        3. 线缆保护
                        - 使用PVC线管保护网线
                        - 接头处做好防水处理
                        - 避免网线受到拉扯

                        【安装步骤】

                        1. 准备工具
                        - 电钻、螺丝刀、水平仪
                        - 膨胀螺丝、支架
                        - 防水胶、绝缘胶带
                        - 网线压线钳（PoE摄像头）

                        2. 固定支架
                        - 使用水平仪确保水平
                        - 墙面打孔深度5-6厘米
                        - 安装膨胀螺丝
                        - 拧紧支架螺丝

                        3. 安装摄像头
                        - 连接网线或电源线
                        - 将摄像头固定在支架上
                        - 调整角度并锁紧
                        - 确保防水尾线密封

                        4. 调试
                        - 通电测试画面
                        - App 查看监控角度
                        - 微调角度到最佳位置
                        - 测试夜视效果

                        【防护措施】

                        1. 防水处理
                        - 检查防水胶圈完整性
                        - 网线入口向下，防止雨水倒灌
                        - 接头处缠绕防水胶带

                        2. 防雷保护
                        - 建议加装防雷器
                        - 避免安装在最高点
                        - 雷雨天气关闭设备

                        3. 防盗设计
                        - 选择不易拆卸的位置
                        - 使用防盗螺丝
                        - 重要位置双机位互相监控

                        【常见问题】

                        Q: 可以透过玻璃拍摄吗？
                        A: 不建议。玻璃会反射红外光，导致夜视失效。如需透过玻璃，选择ColorX或泛光灯型号。

                        Q: 多高的位置不会被偷？
                        A: 3米以上，且使用防盗螺丝。重要位置建议双机位互相监控。

                        Q: WiFi信号不稳定怎么办？
                        A: 1)添加WiFi中继器 2)更换为PoE有线型号 3)更换位置
                        """)
                .source("Reolink 安装指南")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Reolink 监控系统方案设计")
                .content("""
                        不同场景的 Reolink 监控系统设计方案：

                        【方案一：家庭小型监控（预算3000元）】

                        配置清单：
                        - Reolink RLK8-800B4 套装（8路NVR+4个4K摄像头）: ¥3,299

                        部署方案：
                        - 前门：1个 4K PoE 摄像头（监控来访）
                        - 后门：1个 4K PoE 摄像头（防护后院）
                        - 车库：1个 4K PoE 摄像头（车辆监控）
                        - 庭院：1个 4K PoE 摄像头（全景覆盖）

                        特点：
                        - 即插即用，安装简单
                        - 有线连接，稳定可靠
                        - 2TB硬盘，可存储约6-7天录像
                        - 预留4路，可扩展

                        【方案二：别墅全方位监控（预算8000元）】

                        配置清单：
                        - Reolink RLK16-800D8 套装（16路NVR+8个4K摄像头）: ¥6,299
                        - Reolink Argus 4 Pro × 2（补充WiFi位置）: ¥1,798
                        - Reolink Video Doorbell（智能门铃）: ¥599
                        - 4TB 监控硬盘（扩展存储）: ¥600

                        部署方案：
                        - 1楼周边：4个 PoE 4K 摄像头（四个方向）
                        - 2楼周边：4个 PoE 4K 摄像头（四个方向）
                        - 花园死角：2个 Argus 4 Pro WiFi（太阳能供电）
                        - 大门：1个智能门铃
                        - 车库内部：预留1路PoE

                        特点：
                        - 全方位无死角覆盖
                        - 有线+无线混合部署
                        - 4TB硬盘，可存储约12-14天
                        - 智能门铃访客管理

                        【方案三：小型商铺监控（预算5000元）】

                        配置清单：
                        - Reolink RLK8-800B4 套装（8路NVR+4个4K摄像头）: ¥3,299
                        - Reolink TrackMix WiFi（收银区智能追踪）: ¥1,099
                        - Reolink Duo 2 WiFi（出入口双镜头）: ¥759

                        部署方案：
                        - 店铺外：2个 PoE 4K 摄像头（正门+后门）
                        - 店内全景：2个 PoE 4K 摄像头（货架监控）
                        - 收银台：1个 TrackMix（顾客行为追踪）
                        - 店面入口：1个 Duo 2（180°全景无死角）

                        特点：
                        - 重点区域智能追踪
                        - 出入口全景覆盖
                        - 收银区特写抓拍
                        - 可扩展到8路摄像头

                        【方案四：停车场监控（预算12000元）】

                        配置清单：
                        - Reolink RLK16-800D8 套装: ¥6,299
                        - Reolink RLC-810A × 8（额外扩展）: ¥5,592
                        - 6TB 监控硬盘: ¥1,200

                        部署方案：
                        - 出入口：4个 4K 摄像头（车牌识别）
                        - 车位区：12个 4K 摄像头（全覆盖）
                        - 每个摄像头覆盖4-6个车位

                        特点：
                        - 16路全高清监控
                        - 车牌清晰可辨
                        - 6TB存储约20天录像
                        - 夜间红外夜视30米

                        【设计原则】

                        1. 覆盖优先级
                        - 第一优先：出入口、通道
                        - 第二优先：贵重物品区域
                        - 第三优先：死角、边界

                        2. 机位互补
                        - 重要位置双机位
                        - 互相监控防盗
                        - 不同角度证据完整

                        3. 扩展性考虑
                        - NVR 选择大于实际需要
                        - 预留20-30%扩展空间
                        - 网线预留备用路径

                        4. 预算分配
                        - 设备成本：60-70%
                        - 安装成本：15-20%
                        - 配件材料：10-15%
                        - 预留备用：5-10%
                        """)
                .source("Reolink 安装指南")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded Reolink installation guide knowledge base with {} documents", documents.size());
    }
}

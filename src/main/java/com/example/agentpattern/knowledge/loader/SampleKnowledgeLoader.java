package com.example.agentpattern.knowledge.loader;

import com.example.agentpattern.knowledge.base.Document;
import com.example.agentpattern.knowledge.base.KnowledgeBase;
import com.example.agentpattern.knowledge.base.KnowledgeBaseRegistry;
import com.example.agentpattern.knowledge.vector.InMemoryVectorKnowledgeBase;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 示例知识库数据加载器
 * 在应用启动时加载示例数据到知识库
 */
@Slf4j
@Component
public class SampleKnowledgeLoader {

    private final KnowledgeBaseRegistry registry;

    public SampleKnowledgeLoader(KnowledgeBaseRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void loadSampleData() {
        log.info("Loading sample knowledge base data...");

        // 1. 创建产品手册知识库
        loadProductManualKnowledgeBase();

        // 2. 创建技术支持知识库
        loadTechSupportKnowledgeBase();

        // 3. 创建公司政策知识库
        loadCompanyPolicyKnowledgeBase();

        log.info("Sample knowledge base data loaded successfully");
    }

    /**
     * 加载产品手册知识库
     */
    private void loadProductManualKnowledgeBase() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "product-manual",
                "产品使用手册和规格说明"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("iPhone 15 Pro 产品规格")
                .content("""
                        iPhone 15 Pro 主要规格:
                        - 显示屏: 6.1英寸超视网膜XDR显示屏，支持ProMotion自适应刷新率技术(最高120Hz)
                        - 处理器: A17 Pro芯片，6核CPU，6核GPU
                        - 摄像头: 48MP主摄 + 12MP超广角 + 12MP长焦，支持3倍光学变焦
                        - 材质: 钛金属边框，超瓷晶面板
                        - 存储: 128GB/256GB/512GB/1TB
                        - 电池: 视频播放最长可达23小时
                        - 充电: 支持MagSafe无线充电(最高15W)，USB-C接口
                        - 5G: 支持5G网络
                        - 防水: IP68级别防水防尘
                        """)
                .source("产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("MacBook Pro 16 使用指南")
                .content("""
                        MacBook Pro 16英寸 使用指南:
                        - 首次开机: 按下电源键，按照屏幕提示完成设置
                        - 电池保养: 建议保持电池电量在20%-80%之间，避免长期满电或低电量
                        - Touch ID: 可在系统偏好设置中添加指纹，支持快速解锁和Apple Pay
                        - 性能模式: 可在系统设置中选择低功耗模式或高性能模式
                        - 散热: 不要在柔软表面(如床上)使用，确保底部散热孔通畅
                        - 连接显示器: 支持最多4台外接显示器
                        - 键盘背光: Fn键可调节键盘背光亮度
                        """)
                .source("产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("AirPods Pro 配对和使用")
                .content("""
                        AirPods Pro 配对和使用说明:
                        - 首次配对: 打开充电盒盖子，靠近iPhone，按照提示配对
                        - 降噪模式: 长按耳机柄切换主动降噪/通透模式/关闭
                        - 查找我的: 可在"查找"App中定位AirPods位置，播放声音
                        - 电池续航: 单次使用可达6小时(开启降噪)，配合充电盒可达30小时
                        - 充电: 支持无线充电，Lightning接口充电
                        - 耳塞尺寸: 提供小、中、大三种尺寸耳塞，建议进行耳塞贴合度测试
                        - 自动切换: 可在iPhone、iPad、Mac间自动切换
                        - 空间音频: 支持杜比全景声和空间音频
                        """)
                .source("产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Apple Watch Series 9 健康功能")
                .content("""
                        Apple Watch Series 9 健康和健身功能:
                        - 心率监测: 全天候监测心率，支持心率过高/过低提醒
                        - 血氧检测: 后台测量血氧饱和度
                        - 心电图: 通过数码表冠生成心电图
                        - 睡眠追踪: 自动记录睡眠阶段，提供睡眠分析
                        - 摔倒检测: 检测到严重摔倒时自动呼叫紧急联系人
                        - 车祸检测: 检测到严重车祸时自动拨打急救电话
                        - 经期追踪: 记录经期和排卵期
                        - 活动记录: 三个圆环记录活动、锻炼和站立时间
                        - 体能训练: 支持跑步、游泳、瑜伽等多种运动类型
                        """)
                .source("产品手册")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("iPad Air 配件推荐")
                .content("""
                        iPad Air 推荐配件:
                        - Apple Pencil: 支持第二代Apple Pencil，可吸附在iPad侧边充电
                        - 妙控键盘: 带触控板的磁吸键盘，悬浮设计，多角度调节
                        - 智能双面夹: 轻薄保护套，支持多角度站立
                        - USB-C转Lightning转换器: 连接旧款配件
                        - USB-C集线器: 扩展HDMI、USB-A、SD卡槽等接口
                        - 屏幕保护膜: 推荐类纸膜，提升Apple Pencil书写体验
                        - AirTag: 可放在包内防止iPad丢失
                        """)
                .source("产品手册")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded product manual knowledge base with {} documents", documents.size());
    }

    /**
     * 加载技术支持知识库
     */
    private void loadTechSupportKnowledgeBase() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "tech-support",
                "常见技术问题解决方案"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("iPhone无法开机问题")
                .content("""
                        iPhone无法开机的解决方法:
                        1. 强制重启: iPhone 8及以上机型，快速按下并松开音量加键，快速按下并松开音量减键，然后长按侧边按钮直到看到Apple标志
                        2. 充电检查: 连接原装充电器充电至少15分钟，确认充电线和充电器没有损坏
                        3. 屏幕检查: 可能是屏幕问题，尝试听到提示音说明系统正常
                        4. 恢复模式: 连接电脑，打开iTunes或访达，进入恢复模式进行系统恢复
                        5. DFU模式: 如果恢复模式无效，尝试进入DFU模式
                        6. 联系维修: 如果以上方法都无效，可能是硬件故障，建议送修
                        """)
                .source("技术支持")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Mac运行缓慢优化")
                .content("""
                        Mac运行缓慢的优化方法:
                        1. 检查存储空间: 系统偏好设置 > 存储空间，删除不需要的文件
                        2. 关闭开机启动项: 系统偏好设置 > 用户与群组 > 登录项
                        3. 活动监视器: 检查哪些应用占用CPU和内存，结束不必要的进程
                        4. 清理缓存: 可使用CleanMyMac等工具清理系统缓存
                        5. 重置SMC: 关机后按Shift+Control+Option+电源键
                        6. 重置NVRAM: 开机时按Command+Option+P+R，听到两次启动声后松开
                        7. 升级RAM: 如果是可升级机型，考虑增加内存
                        8. 重装系统: 备份数据后全新安装macOS
                        """)
                .source("技术支持")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("AirPods连接问题")
                .content("""
                        AirPods连接问题排查:
                        1. 检查蓝牙: 确保设备蓝牙已开启
                        2. 重新配对: 打开充电盒盖子，长按背面按钮直到指示灯闪白光，然后重新连接
                        3. 忘记设备: 在蓝牙设置中忘记AirPods，然后重新配对
                        4. 更新固件: AirPods连接后会自动更新固件
                        5. 清洁接触点: 用干布轻轻擦拭AirPods和充电盒的金属接触点
                        6. 检查电量: 确保AirPods和充电盒有足够电量
                        7. 重启设备: 重启iPhone或Mac
                        8. 恢复出厂设置: 长按充电盒背面按钮15秒，直到指示灯闪琥珀色
                        """)
                .source("技术支持")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Apple Watch无法配对")
                .content("""
                        Apple Watch无法配对iPhone的解决方法:
                        1. 检查兼容性: 确保iPhone是iOS最新版本，Watch是watchOS最新版本
                        2. 蓝牙和WiFi: 确保iPhone的蓝牙和WiFi都已开启
                        3. 重启设备: 同时重启iPhone和Apple Watch
                        4. 飞行模式: 关闭iPhone的飞行模式
                        5. 距离: 将iPhone和Watch靠近(30厘米内)
                        6. 取消配对: 如果之前配对过，先取消配对然后重新配对
                        7. 网络设置: 重置iPhone网络设置(设置 > 通用 > 还原 > 还原网络设置)
                        8. Watch重置: 设置 > 通用 > 还原 > 抹掉所有内容和设置
                        """)
                .source("技术支持")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded tech support knowledge base with {} documents", documents.size());
    }

    /**
     * 加载公司政策知识库
     */
    private void loadCompanyPolicyKnowledgeBase() {
        KnowledgeBase kb = new InMemoryVectorKnowledgeBase(
                "company-policy",
                "公司服务政策和保修条款"
        );

        List<Document> documents = new ArrayList<>();

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("产品保修政策")
                .content("""
                        产品保修政策详情:
                        - 标准保修: 所有产品享受1年有限保修和90天免费电话技术支持
                        - AppleCare+: 可选购延保服务，iPhone/iPad延长至2年，Mac延长至3年
                        - 保修范围: 涵盖制造缺陷和硬件故障
                        - 不保修情况: 意外损坏、进水、人为损坏、未经授权维修
                        - 电池保修: 电池容量低于80%可免费更换(1年内)
                        - 配件保修: 配件保修期为90天或产品保修期内(以较长者为准)
                        - 维修方式: 送修、上门服务(部分地区)、邮寄维修
                        - 保修查询: 官网输入序列号可查询保修状态
                        """)
                .source("公司政策")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("AppleCare+ 服务计划")
                .content("""
                        AppleCare+ 服务计划说明:
                        - 购买时间: 需在设备购买后60天内购买
                        - 服务期限: iPhone/iPad 2年，Mac 3年
                        - 意外损坏: 每12个月最多2次意外损坏保修，需支付服务费
                        - 屏幕损坏: iPhone服务费188元，iPad服务费378元
                        - 其他损坏: iPhone服务费628元，iPad服务费1588元
                        - Mac维修: 屏幕或外部损坏服务费188元，其他损坏服务费1088元
                        - 电池服务: 电池容量低于80%可免费更换
                        - 优先支持: 享受优先电话和在线技术支持
                        - 全球服务: 在全球任何Apple Store或授权服务商均可享受服务
                        """)
                .source("公司政策")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("以旧换新政策")
                .content("""
                        以旧换新政策:
                        - 适用产品: iPhone、iPad、Mac、Apple Watch
                        - 估价方式: 线上估价或到店评估
                        - 估价因素: 机型、存储容量、成色、功能完好性
                        - 折抵方式: 可抵扣新设备购买价格或获得Apple Store礼品卡
                        - 设备要求: 需能够正常开机，无严重损坏，无iCloud锁
                        - 数据清除: 请提前备份数据并退出Apple ID
                        - 环保回收: 无价值设备也可免费环保回收
                        - 在线办理: 可在官网申请邮寄以旧换新
                        - 线下办理: 所有Apple Store均可办理
                        """)
                .source("公司政策")
                .build());

        documents.add(Document.builder()
                .id(UUID.randomUUID().toString())
                .title("学生教育优惠")
                .content("""
                        学生教育优惠政策:
                        - 适用人群: 大学生、研究生、教职工、新录取学生的家长
                        - 优惠产品: Mac、iPad Pro、iPad Air
                        - 优惠幅度: Mac最高优惠1300元，iPad最高优惠300元
                        - 赠品: 购买Mac或iPad还可获得AirPods(特定促销期间)
                        - 购买限制: 每学年可购买1台Mac、1台iPad
                        - 验证方式: 通过UNiDAYS进行学生身份验证
                        - 购买渠道: Apple官网教育商店、Apple Store教育专区
                        - AppleCare+: 教育优惠价购买AppleCare+
                        """)
                .source("公司政策")
                .build());

        kb.addDocuments(documents);
        registry.registerKnowledgeBase(kb);

        log.info("Loaded company policy knowledge base with {} documents", documents.size());
    }
}

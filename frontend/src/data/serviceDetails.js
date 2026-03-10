// 服务详情数据
// 每个服务的详细信息，包括描述、收费标准、服务特色、服务保障等

export const serviceDetailsData = {
  '日常保洁': {
    name: '日常保洁',
    title: '专业日常保洁',
    billingType: 'hourly',
    price: '120元/小时起',
    duration: '4',
    servedCount: '36740',
    image: '/images/daily-cleaning.png',
    icon: 'Brush',
    applicableScope: '适用于每周一次经常打扫的居家室内。服务范围为居家室内的普通日常保洁，含8大区域保洁，40多项服务内容，详见页面说明、预约须知。',
    description: '专业日常保洁服务，为您提供全面、细致的居家清洁服务。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li><strong>客厅清洁：</strong>地面拖扫、家具擦拭、沙发吸尘、窗户清洁等</li>
        <li><strong>卧室清洁：</strong>床铺整理、衣柜整理、地面清洁、窗户清洁等</li>
        <li><strong>厨房清洁：</strong>灶台清洁、油烟机表面清洁、橱柜擦拭、地面清洁等</li>
        <li><strong>卫生间清洁：</strong>马桶清洁、洗手台清洁、地面清洁、镜面清洁等</li>
        <li><strong>阳台清洁：</strong>地面清洁、栏杆擦拭、窗户清洁等</li>
        <li><strong>其他区域：</strong>门厅、过道、楼梯等公共区域清洁</li>
      </ul>
    `,
    features: [
      '专业团队，经验丰富，服务标准统一',
      '使用专业清洁工具和环保清洁剂',
      '服务流程规范，保证清洁质量',
      '8大区域深度清洁，40多项服务内容',
      '售后保障，服务不满意可申请重做'
    ],
    guarantees: [
      {
        title: '专业认证',
        description: '所有保洁师均经过专业培训和认证，持证上岗'
      },
      {
        title: '质量保证',
        description: '服务不满意可申请重做，确保服务质量达到标准'
      },
      {
        title: '工具齐全',
        description: '配备专业清洁工具和环保清洁剂，无需自备'
      },
      {
        title: '售后保障',
        description: '服务完成后提供售后支持，及时解决后续问题'
      }
    ]
  },
  '餐具处理': {
    name: '餐具处理',
    title: '专业餐具清洗服务',
    billingType: 'hourly',
    price: '80元/小时起',
    duration: '2',
    servedCount: '28560',
    image: '/images/基础家政服务.png',
    icon: 'Tools',
    applicableScope: '适用于家庭日常餐具清洗、消毒、整理服务。包括餐后餐具清洗、消毒柜操作、餐具整理归位等。',
    description: '专业餐具清洗服务，确保餐具干净卫生，使用安心。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>餐后餐具清洗（包括碗、盘、筷子、勺子等）</li>
        <li>餐具消毒处理</li>
        <li>餐具整理归位</li>
        <li>厨房台面清洁</li>
        <li>洗碗池清洁</li>
      </ul>
    `,
    features: [
      '使用专业清洗剂，去油污能力强',
      '高温消毒，确保餐具卫生',
      '餐具分类整理，使用更方便',
      '环保清洗，对人体无害'
    ],
    guarantees: [
      {
        title: '卫生保障',
        description: '使用食品级清洗剂，确保餐具安全卫生'
      },
      {
        title: '消毒处理',
        description: '提供高温消毒服务，杀灭细菌病毒'
      }
    ]
  },
  '管道疏通': {
    name: '管道疏通',
    title: '专业管道疏通服务',
    billingType: 'hourly',
    price: '150元/次起',
    duration: '1',
    servedCount: '15230',
    image: '/images/疏通管道.png',
    icon: 'Link',
    applicableScope: '适用于家庭、办公场所的各类管道疏通服务。包括下水道、马桶、洗手池、地漏等管道疏通。',
    description: '专业管道疏通服务，快速解决管道堵塞问题。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>下水道疏通</li>
        <li>马桶疏通</li>
        <li>洗手池疏通</li>
        <li>地漏疏通</li>
        <li>管道清理和保养</li>
      </ul>
    `,
    features: [
      '专业工具，快速疏通',
      '经验丰富，技术过硬',
      '服务及时，快速响应',
      '保修服务，问题不反复'
    ],
    guarantees: [
      {
        title: '技术保障',
        description: '专业技术人员，使用专业疏通工具'
      },
      {
        title: '保修服务',
        description: '疏通后提供保修期，问题反复免费重做'
      }
    ]
  },
  '衣物护理': {
    name: '衣物护理',
    title: '专业衣物护理服务',
    billingType: 'hourly',
    price: '100元/小时起',
    duration: '2',
    servedCount: '18650',
    image: '/images/services/clothing-care.jpg',
    icon: 'Box',
    applicableScope: '适用于各类衣物的清洗、熨烫、整理服务。包括日常衣物、特殊材质衣物、床上用品等。',
    description: '专业衣物护理服务，让您的衣物焕然一新。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>衣物清洗（手洗、机洗）</li>
        <li>衣物熨烫整理</li>
        <li>特殊材质衣物护理</li>
        <li>衣物分类整理</li>
        <li>衣柜整理</li>
      </ul>
    `,
    features: [
      '根据材质选择合适清洗方式',
      '专业熨烫，衣物平整',
      '分类整理，使用方便',
      '特殊材质专业护理'
    ],
    guarantees: [
      {
        title: '材质保护',
        description: '根据衣物材质选择合适清洗方式，保护衣物'
      },
      {
        title: '质量保证',
        description: '衣物损坏按价赔偿，提供保障'
      }
    ]
  },
  '家居养护': {
    name: '家居养护',
    title: '专业家居养护服务',
    billingType: 'hourly',
    price: '150元/小时起',
    duration: '3',
    servedCount: '12480',
    image: '/images/services/home-maintenance.jpg',
    icon: 'House',
    applicableScope: '适用于各类家具、地板、墙面的清洁养护服务。包括实木家具保养、地板打蜡、墙面清洁等。',
    description: '专业家居养护服务，延长家具使用寿命，保持家居美观。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>实木家具保养（上蜡、抛光）</li>
        <li>地板清洁和打蜡</li>
        <li>墙面清洁</li>
        <li>皮质家具保养</li>
        <li>金属器具清洁保养</li>
      </ul>
    `,
    features: [
      '专业养护剂，保护家具',
      '延长使用寿命',
      '保持家居美观',
      '经验丰富，技术专业'
    ],
    guarantees: [
      {
        title: '专业养护',
        description: '使用专业养护剂，保护家具材质'
      },
      {
        title: '效果保证',
        description: '养护效果显著，家具焕然一新'
      }
    ]
  },
  '月嫂服务': {
    name: '月嫂服务',
    title: '专业月嫂护理服务',
    billingType: 'daily',
    price: '300元/天起',
    duration: '28',
    servedCount: '8560',
    image: '/images/services/maternity-care.jpg',
    icon: 'User',
    applicableScope: '适用于产后28天的专业护理服务。包括产妇护理、新生儿护理、月子餐制作等。',
    description: '专业月嫂服务，为产妇和新生儿提供全方位专业护理。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li><strong>产妇护理：</strong>产后恢复指导、乳房护理、伤口护理、心理疏导等</li>
        <li><strong>新生儿护理：</strong>喂养指导、洗澡抚触、换尿布、睡眠指导等</li>
        <li><strong>月子餐制作：</strong>营养配餐、催乳汤制作、清淡饮食等</li>
        <li><strong>日常家务：</strong>简单家务、整理房间等</li>
      </ul>
    `,
    features: [
      '持证上岗，专业认证',
      '经验丰富，服务贴心',
      '24小时全天候服务',
      '科学护理，专业指导',
      '营养配餐，健康饮食'
    ],
    guarantees: [
      {
        title: '专业认证',
        description: '所有月嫂均持有相关资格证书，经过专业培训'
      },
      {
        title: '健康保障',
        description: '定期健康体检，确保服务人员身体健康'
      },
      {
        title: '服务保障',
        description: '服务不满意可申请更换，确保服务质量'
      }
    ]
  },
  '育儿嫂服务': {
    name: '育儿嫂服务',
    title: '专业育儿嫂服务',
    billingType: 'daily',
    price: '250元/天起',
    duration: '30',
    servedCount: '12350',
    image: '/images/services/babysitter.jpg',
    icon: 'UserFilled',
    applicableScope: '适用于0-3岁婴幼儿的专业护理服务。包括日常照料、早教启蒙、辅食制作等。',
    description: '专业育儿嫂服务，科学育儿，健康成长。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li><strong>日常照料：</strong>喂养、洗澡、换尿布、哄睡等</li>
        <li><strong>早教启蒙：</strong>游戏互动、语言训练、动作训练等</li>
        <li><strong>辅食制作：</strong>营养配餐、辅食制作等</li>
        <li><strong>安全监护：</strong>确保宝宝安全，防止意外发生</li>
      </ul>
    `,
    features: [
      '持证上岗，专业认证',
      '科学育儿，经验丰富',
      '早教启蒙，全面发展',
      '营养配餐，健康成长'
    ],
    guarantees: [
      {
        title: '专业认证',
        description: '所有育儿嫂均持有相关资格证书'
      },
      {
        title: '健康保障',
        description: '定期健康体检，确保服务人员身体健康'
      }
    ]
  },
  '生活照料': {
    name: '生活照料',
    title: '专业生活照料服务',
    billingType: 'daily',
    price: '200元/天起',
    duration: '30',
    servedCount: '9650',
    image: '/images/services/daily-care.jpg',
    icon: 'Reading',
    applicableScope: '适用于老年人的日常照料服务。包括生活起居照料、饮食照料、个人卫生照料等。',
    description: '专业生活照料服务，让老年人生活更舒适安心。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>生活起居照料（协助起床、穿衣、洗漱等）</li>
        <li>饮食照料（制作营养餐、协助进食等）</li>
        <li>个人卫生照料（洗澡、理发、修剪指甲等）</li>
        <li>陪伴聊天、散步等</li>
        <li>简单家务（整理房间、洗衣等）</li>
      </ul>
    `,
    features: [
      '专业护理，经验丰富',
      '贴心服务，耐心细致',
      '健康饮食，营养配餐',
      '陪伴照顾，精神慰藉'
    ],
    guarantees: [
      {
        title: '专业培训',
        description: '所有护理人员均经过专业培训，持证上岗'
      },
      {
        title: '健康保障',
        description: '定期健康体检，确保服务人员身体健康'
      }
    ]
  },
  '健康辅助': {
    name: '健康辅助',
    title: '专业健康辅助服务',
    billingType: 'daily',
    price: '250元/天起',
    duration: '30',
    servedCount: '7420',
    image: '/images/services/health-assistance.jpg',
    icon: 'FirstAidKit',
    applicableScope: '适用于需要健康辅助的人群。包括健康监测、用药提醒、康复训练辅助等。',
    description: '专业健康辅助服务，关爱健康，守护生命。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>健康监测（血压、血糖、体温等）</li>
        <li>用药提醒和协助</li>
        <li>康复训练辅助</li>
        <li>陪同就医</li>
        <li>健康档案管理</li>
      </ul>
    `,
    features: [
      '专业护理，经验丰富',
      '健康监测，及时预警',
      '用药管理，安全可靠',
      '康复辅助，科学训练'
    ],
    guarantees: [
      {
        title: '专业资质',
        description: '具备相关护理资质，经过专业培训'
      },
      {
        title: '安全保障',
        description: '严格操作规程，确保服务安全'
      }
    ]
  },
  '家电维修': {
    name: '家电维修',
    title: '专业家电维修服务',
    billingType: 'times',
    price: '80元/次起',
    duration: '1',
    servedCount: '18560',
    image: '/images/services/appliance-repair.jpg',
    icon: 'Setting',
    applicableScope: '适用于各类家用电器的维修服务。包括冰箱、洗衣机、空调、电视、热水器等。',
    description: '专业家电维修服务，快速解决家电故障问题。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>家电故障检测</li>
        <li>家电维修（更换零件、调试等）</li>
        <li>家电清洁保养</li>
        <li>维修后测试</li>
        <li>保修服务</li>
      </ul>
    `,
    features: [
      '专业技师，技术过硬',
      '原厂配件，质量保证',
      '快速响应，及时维修',
      '保修服务，问题不反复'
    ],
    guarantees: [
      {
        title: '技术保障',
        description: '专业维修技师，多年维修经验'
      },
      {
        title: '配件保障',
        description: '使用原厂配件或同等质量配件'
      },
      {
        title: '保修服务',
        description: '维修后提供保修期，问题反复免费重做'
      }
    ]
  },
  '家电保养': {
    name: '家电保养',
    title: '专业家电保养服务',
    billingType: 'times',
    price: '100元/次起',
    duration: '1',
    servedCount: '12380',
    image: '/images/services/appliance-maintenance.jpg',
    icon: 'Monitor',
    applicableScope: '适用于各类家用电器的定期保养服务。包括空调清洗、油烟机清洗、洗衣机清洗等。',
    description: '专业家电保养服务，延长家电使用寿命，提高使用效率。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>空调清洗保养</li>
        <li>油烟机清洗保养</li>
        <li>洗衣机清洗保养</li>
        <li>热水器清洗保养</li>
        <li>家电检测调试</li>
      </ul>
    `,
    features: [
      '深度清洗，彻底清洁',
      '专业工具，高效保养',
      '延长寿命，提高效率',
      '定期保养，预防故障'
    ],
    guarantees: [
      {
        title: '清洗效果',
        description: '深度清洗，去除污垢，提高使用效率'
      },
      {
        title: '保养效果',
        description: '定期保养，延长家电使用寿命'
      }
    ]
  },
  '小型安装': {
    name: '小型安装',
    title: '专业小型安装服务',
    billingType: 'times',
    price: '100元/次起',
    duration: '1',
    servedCount: '9850',
    image: '/images/services/small-installation.jpg',
    icon: 'Grid',
    applicableScope: '适用于各类小型安装服务。包括灯具安装、窗帘安装、挂件安装、家具组装等。',
    description: '专业小型安装服务，快速完成安装任务。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>灯具安装</li>
        <li>窗帘安装</li>
        <li>挂件安装（画框、镜子等）</li>
        <li>家具组装</li>
        <li>其他小型安装服务</li>
      </ul>
    `,
    features: [
      '专业工具，安装精准',
      '经验丰富，技术过硬',
      '快速完成，不耽误时间',
      '安装后测试，确保正常'
    ],
    guarantees: [
      {
        title: '安装质量',
        description: '安装牢固，符合安全标准'
      },
      {
        title: '测试保障',
        description: '安装后进行测试，确保正常工作'
      }
    ]
  },
  '膳食服务': {
    name: '膳食服务',
    title: '专业膳食服务',
    billingType: 'hourly',
    price: '150元/小时起',
    duration: '2',
    servedCount: '14560',
    image: '/images/services/catering.jpg',
    icon: 'Coffee',
    applicableScope: '适用于家庭膳食制作服务。包括日常三餐制作、营养配餐、特殊饮食需求等。',
    description: '专业膳食服务，营养配餐，健康饮食。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>日常三餐制作</li>
        <li>营养配餐</li>
        <li>特殊饮食需求（低盐、低糖、素食等）</li>
        <li>食材采购建议</li>
        <li>厨房清洁</li>
      </ul>
    `,
    features: [
      '营养配餐，健康饮食',
      '厨艺精湛，口味佳',
      '食材新鲜，安全卫生',
      '根据需求定制食谱'
    ],
    guarantees: [
      {
        title: '卫生保障',
        description: '使用新鲜食材，制作过程卫生规范'
      },
      {
        title: '营养保障',
        description: '科学配餐，营养均衡'
      }
    ]
  },
  '宠物照料': {
    name: '宠物照料',
    title: '专业宠物照料服务',
    billingType: 'hourly',
    price: '100元/小时起',
    duration: '2',
    servedCount: '8650',
    image: '/images/services/pet-care.jpg',
    icon: 'Location',
    applicableScope: '适用于各类宠物的照料服务。包括遛狗、喂食、洗澡、陪伴等。',
    description: '专业宠物照料服务，关爱宠物，细心照料。',
    content: `
      <h4>服务内容包含：</h4>
      <ul>
        <li>宠物喂食</li>
        <li>遛狗服务</li>
        <li>宠物洗澡</li>
        <li>宠物陪伴</li>
        <li>宠物健康监测</li>
      </ul>
    `,
    features: [
      '爱宠人士，细心照料',
      '经验丰富，了解宠物习性',
      '安全可靠，值得信赖',
      '陪伴玩耍，关爱宠物'
    ],
    guarantees: [
      {
        title: '专业照料',
        description: '了解宠物习性，提供专业照料服务'
      },
      {
        title: '安全保障',
        description: '确保宠物安全，防止意外发生'
      }
    ]
  }
}

<template>
  <footer class="app-footer">
    <div class="footer-content">
      <div class="footer-left">
        <div class="contact-info">
          <h4>统一服务热线</h4>
          <p class="phone">0571-888 66666</p>
        </div>
        <div class="address-info">
          <p>地址：浙江省杭州市西湖区文三路398号东信大厦4号楼</p>
          <p>邮编：310013</p>
        </div>
      </div>
      
      <div class="footer-middle">
        <div class="qr-codes">
          <div class="qr-item">
            <div class="qr-code">
              <img src="https://via.placeholder.com/120x120/4CAF50/FFFFFF?text=APP" alt="APP二维码" />
            </div>
            <p>下载三替好生活APP</p>
          </div>
          <div class="qr-item">
            <div class="qr-code">
              <img src="https://via.placeholder.com/120x120/4CAF50/FFFFFF?text=微信" alt="微信二维码" />
            </div>
            <p>微信公众号</p>
          </div>
        </div>
      </div>
      
      <div class="footer-right">
        <div class="footer-links">
          <div class="link-column">
            <h4>走进三替</h4>
            <ul>
              <li>集团简介</li>
              <li>发展历程</li>
              <li>董事长致辞</li>
              <li>企业文化</li>
              <li>领导题词</li>
              <li>三替荣誉</li>
            </ul>
          </div>
          <div class="link-column">
            <h4>集团动态</h4>
            <ul>
              <li>集团新闻</li>
              <li>行业资讯</li>
              <li>媒体报道</li>
              <li>公益活动</li>
            </ul>
          </div>
          <div class="link-column">
            <h4>产品服务</h4>
            <ul>
              <li>清洁保养</li>
              <li>疏通维修</li>
              <li>搬家搬场</li>
              <li>家务管家</li>
              <li>母婴护理</li>
              <li>健康与养老</li>
            </ul>
          </div>
          <div class="link-column">
            <h4>加入三替</h4>
            <ul>
              <li>员工招募</li>
              <li>合作共赢</li>
            </ul>
          </div>
          <div class="link-column">
            <h4>联系我们</h4>
            <ul>
              <li>联系方式</li>
              <li>线下门店</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <p>&copy; 2024 家政服务预约平台 版权所有</p>
    </div>
    
    <!-- 右侧固定按钮 -->
    <div class="fixed-sidebar">
      <div class="sidebar-item" @click="scrollToTop">
        <el-icon><ArrowUp /></el-icon>
        <span>返回顶部</span>
      </div>
      <div class="sidebar-item">
        <el-icon><Download /></el-icon>
        <span>APP下载</span>
      </div>
      <div class="sidebar-item">
        <el-icon><Phone /></el-icon>
        <span>咨询电话</span>
      </div>
      <div class="sidebar-item">
        <el-icon><ChatDotRound /></el-icon>
        <span>在线沟通</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ArrowUp, Download, Phone, ChatDotRound } from '@element-plus/icons-vue'

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}
</script>

<style scoped lang="scss">
.app-footer {
  background-color: #2c2c2c;
  color: #fff;
  padding: 40px 20px 20px;
  position: relative;
  margin-top: 60px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 250px 200px 1fr;
  gap: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #444;
  
  @media (max-width: 992px) {
    grid-template-columns: 1fr;
    gap: 30px;
  }
}

.footer-left {
  .contact-info {
    margin-bottom: 20px;
    
    h4 {
      font-size: 16px;
      margin: 0 0 10px 0;
      color: #fff;
    }
    
    .phone {
      font-size: 24px;
      color: #4CAF50;
      font-weight: bold;
      margin: 0;
    }
  }
  
  .address-info {
    p {
      font-size: 14px;
      color: #ccc;
      margin: 5px 0;
      line-height: 1.6;
    }
  }
}

.footer-middle {
  .qr-codes {
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .qr-item {
      text-align: center;
      
      .qr-code {
        width: 120px;
        height: 120px;
        background: white;
        padding: 5px;
        border-radius: 4px;
        margin: 0 auto 10px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }
      
      p {
        font-size: 12px;
        color: #ccc;
        margin: 0;
      }
    }
  }
}

.footer-right {
  .footer-links {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 20px;
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .link-column {
      h4 {
        font-size: 16px;
        margin: 0 0 15px 0;
        color: #fff;
        border-left: 3px solid #4CAF50;
        padding-left: 10px;
      }
      
      ul {
        list-style: none;
        padding: 0;
        margin: 0;
        
        li {
          font-size: 14px;
          color: #ccc;
          margin-bottom: 8px;
          cursor: pointer;
          transition: color 0.3s;
          
          &:hover {
            color: #4CAF50;
          }
        }
      }
    }
  }
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  
  p {
    font-size: 14px;
    color: #999;
    margin: 0;
  }
}

.fixed-sidebar {
  position: fixed;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  
  .sidebar-item {
    width: 50px;
    height: 60px;
    background-color: #4CAF50;
    color: white;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: background-color 0.3s;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    &:hover {
      background-color: #45a049;
    }
    
    &:last-child {
      border-bottom: none;
    }
    
    .el-icon {
      font-size: 20px;
      margin-bottom: 5px;
    }
    
    span {
      font-size: 12px;
    }
  }
}
</style>


<template>
  <div class="my-schedule-view">
    <Navigation />
    <div class="content">
      <el-container>
        <el-main>
          <el-card>
            <template #header>
              <h2>工作日程管理</h2>
            </template>
            
            <el-tabs v-model="activeMainTab">
              <!-- 日历视图 -->
              <el-tab-pane label="日历视图" name="calendar">
            <el-calendar v-model="currentDate">
              <template #date-cell="{ data }">
                <div class="calendar-day">
                  <div class="day-number">{{ data.day.split('-').slice(2).join('-') }}</div>
                      <div class="day-events">
                        <!-- 可服务时间 -->
                        <el-tag
                          v-for="schedule in getSchedulesByDate(data.day)"
                          :key="`schedule-${schedule.scheduleId}`"
                          size="small"
                          type="success"
                          style="margin: 2px; display: block;"
                        >
                          {{ formatTime(schedule.startTime) }}-{{ formatTime(schedule.endTime) }}
                        </el-tag>
                        <!-- 预约信息 -->
                    <el-tag
                      v-for="appointment in getAppointmentsByDate(data.day)"
                          :key="`appointment-${appointment.appointmentId}`"
                      size="small"
                          :type="getAppointmentTagType(appointment.status)"
                          style="margin: 2px; display: block;"
                    >
                          {{ formatAppointmentTime(appointment) }}
                    </el-tag>
                  </div>
                </div>
              </template>
            </el-calendar>
              </el-tab-pane>
            
              <!-- 预约列表 -->
              <el-tab-pane label="预约列表" name="appointments">
                <el-tabs v-model="activeAppointmentTab">
              <el-tab-pane label="待接单" name="pending">
                    <el-table :data="pendingAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="staffName" label="服务人员" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        link
                        @click="viewAppointmentDetail(scope.row.appointmentId)"
                      >
                        查看详情
                      </el-button>
                      <el-button
                        type="success"
                        size="small"
                            @click="confirmAppointment(scope.row.appointmentId)"
                      >
                        接单
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                            @click="rejectAppointment(scope.row.appointmentId)"
                      >
                        拒绝
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="已接单" name="confirmed">
                    <el-table :data="confirmedAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="staffName" label="服务人员" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="150">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <el-tag :type="getAppointmentTagType(scope.row.status)" size="small">
                              {{ getStatusText(scope.row.status) }}
                            </el-tag>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        link
                        @click="viewAppointmentDetail(scope.row.appointmentId)"
                      >
                        查看详情
                      </el-button>
                      <el-button
                        type="primary"
                        size="small"
                        :disabled="scope.row.paymentStatus !== PAYMENT_STATUS.PAID"
                            @click="startService(scope.row.appointmentId)"
                      >
                        开始服务
                      </el-button>
                      <el-tooltip v-if="scope.row.paymentStatus !== PAYMENT_STATUS.PAID" content="订单未支付，无法开始服务" placement="top">
                        <el-icon style="margin-left: 5px; color: #f56c6c;"><Warning /></el-icon>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="服务中" name="in_service">
                    <el-table :data="inServiceAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="staffName" label="服务人员" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="180">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <div style="font-size: 12px; color: #666;">
                              <div>状态: {{ getStatusText(scope.row.status) }}</div>
                              <div v-if="scope.row.startTime" style="margin-top: 4px;">
                                开始: {{ formatTime(scope.row.startTime) }}
                              </div>
                            </div>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="200" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        link
                        @click="viewAppointmentDetail(scope.row.appointmentId)"
                      >
                        查看详情
                      </el-button>
                      <el-button
                        type="success"
                        size="small"
                        :disabled="scope.row.paymentStatus !== PAYMENT_STATUS.PAID"
                            @click="completeService(scope.row.appointmentId)"
                      >
                        完成服务
                      </el-button>
                      <el-tooltip v-if="scope.row.paymentStatus !== PAYMENT_STATUS.PAID" content="订单未支付，无法结束服务" placement="top">
                        <el-icon style="margin-left: 5px; color: #f56c6c;"><Warning /></el-icon>
                      </el-tooltip>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              
              <el-tab-pane label="已完成" name="completed">
                    <el-table :data="completedAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="staffName" label="服务人员" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="200">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <div style="font-size: 12px; color: #666;">
                              <div>状态: {{ getStatusText(scope.row.status) }}</div>
                              <div v-if="scope.row.startTime" style="margin-top: 4px;">
                                开始: {{ formatTime(scope.row.startTime) }}
                              </div>
                              <div v-if="scope.row.endTime" style="margin-top: 4px;">
                                结束: {{ formatTime(scope.row.endTime) }}
                              </div>
                            </div>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="120" fixed="right">
                        <template #default="scope">
                          <el-button
                            type="primary"
                            size="small"
                            link
                            @click="viewAppointmentDetail(scope.row.appointmentId)"
                          >
                            查看详情
                          </el-button>
                        </template>
                      </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 已拒单/已关闭 -->
              <el-tab-pane label="已拒单" name="rejected">
                    <el-table :data="rejectedAppointments" border>
                      <el-table-column prop="appointmentId" label="预约ID" width="100" />
                      <el-table-column prop="customerName" label="客户姓名" width="120" />
                      <el-table-column prop="staffName" label="服务人员" width="120" />
                      <el-table-column prop="serviceName" label="服务项目" width="150" />
                      <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                      <el-table-column label="预约时间" width="200">
                        <template #default="scope">
                          {{ formatAppointmentTime(scope.row) }}
                        </template>
                      </el-table-column>
                      <el-table-column prop="serviceAddress" label="服务地址" show-overflow-tooltip />
                      <el-table-column prop="totalAmount" label="金额" width="100">
                        <template #default="scope">
                          ¥{{ scope.row.totalAmount || 0 }}
                        </template>
                      </el-table-column>
                      <el-table-column label="支付状态" width="100">
                        <template #default="scope">
                          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
                            {{ getPaymentStatusText(scope.row.paymentStatus) }}
                          </el-tag>
                        </template>
                      </el-table-column>
                      <el-table-column label="状态跟踪" width="200">
                        <template #default="scope">
                          <el-tooltip :content="getStatusTrackingText(scope.row)" placement="top">
                            <div style="font-size: 12px; color: #666;">
                              <div>状态: {{ getStatusText(scope.row.status) }}</div>
                              <div v-if="scope.row.startTime" style="margin-top: 4px;">
                                开始: {{ formatTime(scope.row.startTime) }}
                              </div>
                              <div v-if="scope.row.endTime" style="margin-top: 4px;">
                                结束: {{ formatTime(scope.row.endTime) }}
                              </div>
                            </div>
                          </el-tooltip>
                        </template>
                      </el-table-column>
                      <el-table-column label="操作" width="160" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="primary"
                        size="small"
                        link
                        @click="viewAppointmentDetail(scope.row.appointmentId)"
                      >
                        查看详情
                      </el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </el-tab-pane>
                </el-tabs>
              </el-tab-pane>
              
              <!-- 可服务时间 -->
              <el-tab-pane label="可服务时间" name="schedules">
                <div class="schedules-section">
                  <div class="section-header">
                    <h3>可服务时间列表</h3>
                    <el-button type="primary" @click="showAddScheduleDialog = true">
                      <el-icon><Plus /></el-icon>
                      添加可服务时间
                    </el-button>
                  </div>
                  
                  <el-table :data="schedulesList" border style="margin-top: 20px;">
                    <el-table-column prop="workDate" label="日期" width="120" />
                    <el-table-column label="时间" width="200">
                      <template #default="scope">
                        {{ formatTime(scope.row.startTime) }} - {{ formatTime(scope.row.endTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="availableStatus" label="状态" width="100">
                      <template #default="scope">
                        <el-tag :type="scope.row.availableStatus === 1 ? 'success' : 'danger'">
                          {{ scope.row.availableStatus === 1 ? '可服务' : '不可服务' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="150">
                      <template #default="scope">
                        <el-button size="small" @click="editSchedule(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="deleteScheduleItem(scope.row.scheduleId)">
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                </el-table>
                </div>
              </el-tab-pane>

              <!-- 我的评价（已完成订单 + 顾客评价） -->
              <el-tab-pane label="我的评价" name="my_reviews">
                <div class="reviews-section">
                  <div class="section-header">
                    <h3>我的评价</h3>
                    <el-button type="primary" @click="loadMyReviews" :loading="myReviewsLoading">
                      刷新
                    </el-button>
                  </div>

                  <el-alert
                    title="展示已完成订单对应的顾客评价（评分与评价内容）。先展示订单信息，再展示评价内容。"
                    type="info"
                    :closable="false"
                    style="margin: 10px 0 16px;"
                    show-icon
                  />

                  <el-table :data="myReviewRows" border v-loading="myReviewsLoading">
                    <el-table-column prop="appointmentId" label="预约ID" width="100" />
                    <el-table-column prop="serviceName" label="服务项目" width="150" show-overflow-tooltip />
                    <el-table-column prop="customerName" label="客户姓名" width="120" />
                    <el-table-column prop="appointmentDate" label="预约日期" width="120" />
                    <el-table-column label="预约时间" width="200">
                      <template #default="scope">
                        {{ formatAppointmentTime(scope.row) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="totalAmount" label="金额" width="100">
                      <template #default="scope">
                        ¥{{ scope.row.totalAmount || 0 }}
                      </template>
                    </el-table-column>

                    <el-table-column label="评价状态" width="110" align="center">
                      <template #default="scope">
                        <el-tag :type="scope.row.hasReview ? 'success' : 'info'">
                          {{ scope.row.hasReview ? '已评价' : '未评价' }}
                        </el-tag>
                      </template>
                    </el-table-column>

                    <el-table-column label="评分" width="160" align="center">
                      <template #default="scope">
                        <div v-if="scope.row.hasReview">
                          <el-rate v-model="scope.row.overallRating" disabled show-score text-color="#ff9900" />
                        </div>
                        <span v-else style="color:#999;">-</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="评价内容" min-width="220" show-overflow-tooltip>
                      <template #default="scope">
                        <span v-if="scope.row.hasReview">{{ scope.row.reviewContent || '（无）' }}</span>
                        <span v-else style="color:#999;">-</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="评价时间" width="170">
                      <template #default="scope">
                        <span v-if="scope.row.hasReview">{{ scope.row.reviewTime || '-' }}</span>
                        <span v-else style="color:#999;">-</span>
                      </template>
                    </el-table-column>

                    <el-table-column label="操作" width="180" fixed="right">
                      <template #default="scope">
                        <el-button type="primary" size="small" link @click="viewAppointmentDetail(scope.row.appointmentId)">
                          查看订单
                        </el-button>
                        <el-button type="success" size="small" link @click="openReviewDetail(scope.row)">
                          查看评价
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>

                  <el-empty
                    v-if="!myReviewsLoading && myReviewRows.length === 0"
                    description="暂无已完成订单或评价记录"
                    style="margin-top: 20px;"
                  />
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-main>
      </el-container>
    </div>
    
    <!-- 添加/编辑可服务时间对话框 -->
    <el-dialog
      v-model="showAddScheduleDialog"
      :title="editingSchedule ? '编辑可服务时间' : '添加可服务时间'"
      width="500px"
    >
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="120px">
        <el-form-item label="日期" prop="workDate">
          <el-date-picker
            v-model="scheduleForm.workDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledPastDate"
          />
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="scheduleForm.startTime"
            placeholder="选择开始时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endDateTime">
          <el-date-picker
            v-model="scheduleForm.endDateTime"
            type="datetime"
            placeholder="选择结束日期和时间（同一天，不支持跨天）"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            :disabled-date="disabledPastDate"
            :default-time="scheduleForm.startTime ? [new Date(`2000-01-01 ${scheduleForm.startTime}`)] : undefined"
          />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            提示：根据最新业务规则，排班暂不支持跨天，如需跨天请拆分为多条记录
          </div>
        </el-form-item>
        
        <el-form-item label="状态" prop="availableStatus">
          <el-radio-group v-model="scheduleForm.availableStatus">
            <el-radio :value="1">可服务</el-radio>
            <el-radio :value="0">不可服务</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-alert
          v-if="conflictWarning"
          :title="conflictWarning"
          type="warning"
          :closable="false"
          style="margin-bottom: 20px;"
        />
      </el-form>
      
      <template #footer>
        <el-button @click="showAddScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 预约详情对话框（服务人员端） -->
    <el-dialog
      v-model="detailDialogVisible"
      title="预约详情"
      width="820px"
      @close="appointmentDetail = null"
    >
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border v-if="appointmentDetail">
          <el-descriptions-item label="预约ID">
            {{ appointmentDetail.appointmentId }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getAppointmentTagType(appointmentDetail.status)">
              {{ getStatusText(appointmentDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户ID">
            {{ appointmentDetail.customerId || appointmentDetail.customer_id || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">
            {{ appointmentDetail.customerName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系方式">
            {{ appointmentDetail.customerPhone || appointmentDetail.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="服务人员">
            {{ appointmentDetail.staffName || staffDisplayName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="服务项目">
            {{ appointmentDetail.serviceName || appointmentDetail.service_name || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="预约日期">
            {{ appointmentDetail.appointmentDate || appointmentDetail.appointment_date || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatAppointmentTime(appointmentDetail) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务地址" :span="2">
            {{ appointmentDetail.serviceAddress || appointmentDetail.service_address || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ appointmentDetail.remarks || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            ¥{{ appointmentDetail.totalAmount || appointmentDetail.total_amount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(appointmentDetail.paymentStatus)">
              {{ getPaymentStatusText(appointmentDetail.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ appointmentDetail.createTime || appointmentDetail.create_time || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ appointmentDetail.updateTime || appointmentDetail.update_time || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 评价详情对话框（先订单后评价，展示三方互评） -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="订单评价详情"
      width="820px"
      @close="selectedReviewRow = null"
    >
      <div v-if="selectedReviewRow">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约ID">
            {{ selectedReviewRow.appointmentId }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getAppointmentTagType(selectedReviewRow.status)">
              {{ getStatusText(selectedReviewRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">
            {{ selectedReviewRow.customerName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="服务项目">
            {{ selectedReviewRow.serviceName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="预约日期">
            {{ selectedReviewRow.appointmentDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatAppointmentTime(selectedReviewRow) }}
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            ¥{{ selectedReviewRow.totalAmount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(selectedReviewRow.paymentStatus)">
              {{ getPaymentStatusText(selectedReviewRow.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <!-- 顾客评价块 -->
        <h4 style="margin: 10px 0;">顾客对本次服务的评价</h4>
        <div v-if="customerReviewBlock">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总体评分">
              <el-rate v-model="customerReviewBlock.overallRating" disabled show-score text-color="#ff9900" />
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">
              {{ customerReviewBlock.reviewTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="服务态度" v-if="customerReviewBlock.serviceAttitudeRating !== null && customerReviewBlock.serviceAttitudeRating !== undefined">
              <el-rate v-model="customerReviewBlock.serviceAttitudeRating" disabled />
              <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.serviceAttitudeRating }}分</span>
            </el-descriptions-item>
            <el-descriptions-item label="专业能力" v-if="customerReviewBlock.professionalAbilityRating !== null && customerReviewBlock.professionalAbilityRating !== undefined">
              <el-rate v-model="customerReviewBlock.professionalAbilityRating" disabled />
              <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.professionalAbilityRating }}分</span>
            </el-descriptions-item>
            <el-descriptions-item label="服务质量" v-if="customerReviewBlock.serviceQualityRating !== null && customerReviewBlock.serviceQualityRating !== undefined">
              <el-rate v-model="customerReviewBlock.serviceQualityRating" disabled />
              <span style="margin-left:8px;color:#666;">{{ customerReviewBlock.serviceQualityRating }}分</span>
            </el-descriptions-item>
            <el-descriptions-item label="评价内容" :span="2">
              {{ customerReviewBlock.reviewContent || '（无）' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <el-empty v-else description="顾客暂未评价" />

        <el-divider />

        <!-- 服务人员自评块 -->
        <h4 style="margin: 10px 0;">我对本次服务的自评</h4>
        <div v-if="staffSelfReviewBlock">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总体评分">
              <el-rate v-model="staffSelfReviewBlock.overallRating" disabled show-score text-color="#ff9900" />
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">
              {{ staffSelfReviewBlock.reviewTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="自评内容" :span="2">
              {{ staffSelfReviewBlock.reviewContent || '（无）' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else>
          <el-alert
            v-if="canCreateSelfReview"
            title="您还没有对本次服务进行自评，可以在下面打分并填写自评内容。"
            type="info"
            :closable="false"
            style="margin-bottom: 10px;"
          />
          <el-form
            v-if="canCreateSelfReview"
            :model="selfReviewForm"
            label-width="90px"
            style="max-width: 600px;"
          >
            <el-form-item label="总体评分">
              <el-rate v-model="selfReviewForm.overallRating" show-score />
            </el-form-item>
            <el-form-item label="自评内容">
              <el-input
                v-model="selfReviewForm.reviewContent"
                type="textarea"
                :rows="3"
                placeholder="请简单描述本次服务的情况、做得好的地方以及需要改进的地方"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitSelfReview" :loading="selfReviewSubmitting">
                提交自评
              </el-button>
            </el-form-item>
          </el-form>
          <el-empty v-else description="暂不支持自评（订单未完成或未支付）" />
        </div>

        <el-divider />

        <!-- 管理员评价块（简要展示，便于服务人员看到公司反馈） -->
        <h4 style="margin: 10px 0;">管理员评价</h4>
        <div class="admin-review-blocks">
          <div class="admin-review-item">
            <h5>管理员对顾客的评价</h5>
            <div v-if="adminReviewForCustomerBlock">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="总体评分">
                  <el-rate v-model="adminReviewForCustomerBlock.overallRating" disabled show-score text-color="#ff9900" />
                </el-descriptions-item>
                <el-descriptions-item label="评价时间">
                  {{ adminReviewForCustomerBlock.reviewTime || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="评价内容" :span="2">
                  {{ adminReviewForCustomerBlock.reviewContent || '（无）' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="管理员暂未评价顾客" />
          </div>

          <div class="admin-review-item">
            <h5>管理员对服务人员的评价</h5>
            <div v-if="adminReviewForStaffBlock">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="总体评分">
                  <el-rate v-model="adminReviewForStaffBlock.overallRating" disabled show-score text-color="#ff9900" />
                </el-descriptions-item>
                <el-descriptions-item label="评价时间">
                  {{ adminReviewForStaffBlock.reviewTime || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="评价内容" :span="2">
                  {{ adminReviewForStaffBlock.reviewContent || '（无）' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <el-empty v-else description="管理员暂未评价服务人员" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Warning } from '@element-plus/icons-vue'
import { 
  getStaffAppointmentsList,
  getStaffAppointmentDetail,
  acceptAppointment,
  rejectAppointment as rejectAppointmentAPI,
  startAppointment,
  completeAppointment
} from '@/api/appointments'
import { getSchedules, createSchedule, updateSchedule, deleteSchedule } from '@/api/schedules'
import { getUserInfo } from '@/api/user'
import { getStaffProfile } from '@/api/staff'
import * as reviewsApi from '@/api/reviews'
import { queryPaymentStatusByAppointment } from '@/api/payment'
import { PAYMENT_STATUS_CONFIG, PAYMENT_STATUS } from '@/utils/constants'
import Navigation from '@/components/common/Navigation.vue'
import dayjs from 'dayjs'

const store = useStore()

const currentDate = ref(new Date())
const activeMainTab = ref('calendar')
const activeAppointmentTab = ref('pending')
const appointments = ref([])
const schedulesList = ref([])
const showAddScheduleDialog = ref(false)
const editingSchedule = ref(null)
const saving = ref(false)
const conflictWarning = ref('')
const scheduleFormRef = ref(null)

// 注意：store.getters['user/userId'] 是登录用户ID，不一定等于 staffId（服务人员实体ID）
const staffId = computed(() => store.getters['user/userId'])
const staffDisplayName = computed(() => store.getters['user/userInfo']?.name || store.getters['user/userInfo']?.username || '')
const staffEntityId = ref(null)

// 预约详情弹窗
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const appointmentDetail = ref(null)

// 我的评价 tab
const myReviewsLoading = ref(false)
const myReviewRows = ref([])
const reviewDialogVisible = ref(false)
const selectedReviewRow = ref(null)
const customerReviewBlock = ref(null)
const staffSelfReviewBlock = ref(null)
const adminReviewForCustomerBlock = ref(null)
const adminReviewForStaffBlock = ref(null)
const selfReviewForm = reactive({
  overallRating: 0,
  reviewContent: ''
})
const selfReviewSubmitting = ref(false)

// 缓存：customerId -> username
const customerNameCache = new Map()
const isBlank = (v) => v === null || v === undefined || String(v).trim() === ''

const pickId = (row, keys) => {
  for (const k of keys) {
    const val = row?.[k]
    if (!isBlank(val)) return val
  }
  return null
}

const fetchCustomerUsername = async (customerId) => {
  const id = Number(customerId)
  if (!Number.isFinite(id)) return ''
  if (customerNameCache.has(id)) return customerNameCache.get(id)
  try {
    const res = await getUserInfo(id)
    const data = res.data?.data || res.data || {}
    const username = data.username || data.userName || data.name || ''
    customerNameCache.set(id, username)
    return username
  } catch (e) {
    customerNameCache.set(id, '')
    return ''
  }
}

const enrichAppointmentNames = async (list) => {
  if (!Array.isArray(list) || list.length === 0) return

  const needCustomerIds = new Set()
  for (const row of list) {
    const cid = pickId(row, ['customerId', 'customer_id', 'userId', 'user_id'])
    if (isBlank(row.customerName) && !isBlank(cid)) needCustomerIds.add(Number(cid))
    // 服务人员端列表一般不会返回 staffName，这里统一补上当前登录人员名字
    if (isBlank(row.staffName) && staffDisplayName.value) row.staffName = staffDisplayName.value
  }

  const tasks = []
  for (const id of needCustomerIds) tasks.push(fetchCustomerUsername(id))
  await Promise.allSettled(tasks)

  for (const row of list) {
    const cid = pickId(row, ['customerId', 'customer_id', 'userId', 'user_id'])
    if (isBlank(row.customerName) && !isBlank(cid)) {
      row.customerName = customerNameCache.get(Number(cid)) || row.customerName || ''
    }
    if (isBlank(row.staffName) && staffDisplayName.value) row.staffName = staffDisplayName.value
  }
}

const resolveStaffEntityId = async () => {
  if (staffEntityId.value) return staffEntityId.value

  const userInfo = store.getters['user/userInfo'] || {}
  // ⚠️ 注意：userInfo.id 是登录用户ID（userId），通常不等于 staffId（服务人员实体ID）。
  // 这里必须优先取 staffId；取不到就调用 getStaffProfile()，最后才兜底用 userId。
  const fromUserInfo =
    userInfo.staff?.staffId ||
    userInfo.staff?.staff_id ||
    userInfo.staffId ||
    userInfo.staff_id

  const parsed = Number(fromUserInfo)
  if (Number.isFinite(parsed) && parsed > 0) {
    staffEntityId.value = parsed
    return staffEntityId.value
  }

  // 再兜底：调服务人员资料接口拿 staffId
  try {
    const res = await getStaffProfile()
    const data = res.data?.data || res.data || {}
    const sid = data.staffId || data.staff_id || data.id
    const n = Number(sid)
    if (Number.isFinite(n) && n > 0) {
      staffEntityId.value = n
      return staffEntityId.value
    }
  } catch (e) {
    // ignore
  }

  // 最后兜底：如果后端确实用 userId 作为 staffId（少见），才使用 userInfo.id
  const fallback = Number(userInfo.id)
  if (Number.isFinite(fallback) && fallback > 0) {
    staffEntityId.value = fallback
    return staffEntityId.value
  }
  return null
}

const resetReviewBundleState = () => {
  customerReviewBlock.value = null
  staffSelfReviewBlock.value = null
  adminReviewForCustomerBlock.value = null
  adminReviewForStaffBlock.value = null
  selfReviewForm.overallRating = 0
  selfReviewForm.reviewContent = ''
}

const loadReviewBundle = async (appointmentId) => {
  try {
    const fn = reviewsApi.getStaffAppointmentReviewBundle
    if (typeof fn !== 'function') {
      // 兼容老版本：如果接口未提供，则直接跳过，不影响“我的评价”主列表
      return
    }
    const res = await fn(appointmentId)
    const payload = res.data?.data || res.data || {}
    const nr = (r) => (r ? normalizeReview(r) : null)
    customerReviewBlock.value = nr(payload.customerReview)
    staffSelfReviewBlock.value = nr(payload.staffSelfReview)
    adminReviewForCustomerBlock.value = nr(payload.adminReviewForCustomer)
    adminReviewForStaffBlock.value = nr(payload.adminReviewForStaff)
  } catch (e) {
    // 兼容后端尚未完全支持 bundle 接口的情况，不阻塞对话框打开
    console.error('加载三方评价包失败', e)
  }
}

const openReviewDetail = async (row) => {
  selectedReviewRow.value = row
  resetReviewBundleState()
  if (row?.appointmentId) {
    await loadReviewBundle(row.appointmentId)
  }
  reviewDialogVisible.value = true
}

const canCreateSelfReview = computed(() => {
  if (!selectedReviewRow.value) return false
  const statusOk = Number(selectedReviewRow.value.status) === 3
  const paidOk = Number(selectedReviewRow.value.paymentStatus) === PAYMENT_STATUS.PAID
  const hasSelfReview = !!staffSelfReviewBlock.value
  return statusOk && paidOk && !hasSelfReview
})

const submitSelfReview = async () => {
  if (!selectedReviewRow.value) return
  const appointmentId = selectedReviewRow.value.appointmentId
  if (!appointmentId) {
    ElMessage.error('预约ID缺失，无法提交自评')
    return
  }
  if (!selfReviewForm.overallRating) {
    ElMessage.warning('请先给本次服务打一个总体评分')
    return
  }
  if (selfReviewSubmitting.value) return
  selfReviewSubmitting.value = true
  try {
    // 直接调用后端自评接口，如果后端未实现，会在 catch 中提示“提交自评失败”
    await reviewsApi.createStaffSelfReview({
      appointmentId,
      overallRating: selfReviewForm.overallRating,
      reviewContent: selfReviewForm.reviewContent
    })
    ElMessage.success('自评提交成功')
    // 重新加载三方评价包和“我的评价”列表
    await loadReviewBundle(appointmentId)
    await loadMyReviews()
  } catch (e) {
    const msg = e.response?.data?.message || '提交自评失败'
    ElMessage.error(msg)
  } finally {
    selfReviewSubmitting.value = false
  }
}

const normalizeReview = (r) => {
  if (!r) return null
  const appointmentId = r.appointmentId ?? r.appointment_id ?? r.orderId ?? r.order_id ?? null
  return {
    appointmentId: appointmentId !== null ? Number(appointmentId) : null,
    reviewId: r.reviewId ?? r.id ?? r.review_id ?? null,
    overallRating: r.overallRating ?? r.rating ?? r.overall_rating ?? null,
    serviceAttitudeRating: r.serviceAttitudeRating ?? r.service_attitude_rating ?? null,
    professionalAbilityRating: r.professionalAbilityRating ?? r.professional_ability_rating ?? null,
    serviceQualityRating: r.serviceQualityRating ?? r.service_quality_rating ?? null,
    reviewContent: r.reviewContent ?? r.content ?? r.review_content ?? null,
    reviewTime: r.createTime ?? r.createdAt ?? r.created_at ?? r.reviewTime ?? null
  }
}

const loadMyReviews = async () => {
  myReviewsLoading.value = true
  try {
    // 保证订单是最新的
    await loadAppointments()

    const sid = await resolveStaffEntityId()
    if (!sid) {
      ElMessage.warning('无法获取服务人员ID，暂无法加载评价列表')
      myReviewRows.value = []
      return
    }

    // 优先使用“当前登录用户的评价列表”接口，避免 staffId/userId 不一致导致查不到
    let reviewRes = null
    try {
      const getMyReviews = reviewsApi.getMyReviews
      if (!getMyReviews) {
        throw new Error('当前环境未提供 getMyReviews 接口')
      }
      reviewRes = await getMyReviews()
    } catch (e) {
      // fallback to old staffId-based api
      const getReviewsByStaff = reviewsApi.getReviewsByStaff
      if (!getReviewsByStaff) {
        throw e
      }
      reviewRes = await getReviewsByStaff(sid)
    }
    let rawReviewData = reviewRes.data

    // 兼容多种后端返回结构：{ code, data: [] } / { data: { records/list/items/rows: [] } } / 直接数组
    const extractList = (payload) => {
      if (!payload) return []
      if (Array.isArray(payload)) return payload

      const d = payload.data ?? payload
      if (Array.isArray(d)) return d
      if (Array.isArray(d.records)) return d.records
      if (Array.isArray(d.list)) return d.list
      if (Array.isArray(d.items)) return d.items
      if (Array.isArray(d.rows)) return d.rows
      if (Array.isArray(d.content)) return d.content
      return []
    }

    const reviewList = extractList(rawReviewData)

    if (import.meta.env.DEV) {
      console.log('📥 服务人员评价原始返回:', reviewRes.data)
      console.log('🧩 解析评价使用的 staffEntityId:', sid)
      console.log(`✅ 解析出 ${reviewList.length} 条评价记录`)
    }

    const map = new Map()
    for (const r of reviewList) {
      const nr = normalizeReview(r)
      if (nr?.appointmentId) {
        map.set(Number(nr.appointmentId), nr)
      }
    }

    // 以“已完成订单”为主，关联评价
    const completed = (appointments.value || []).filter(a => Number(a.status) === 3)
    await enrichAppointmentNames(completed)

    myReviewRows.value = completed
      .map(a => {
        const rawId = a.appointmentId ?? a.id ?? a.appointment_id
        const id = Number(rawId)
        const review = map.get(id)
        return {
          ...a,
          appointmentId: rawId,
          hasReview: !!review,
          overallRating: review?.overallRating ?? null,
          serviceAttitudeRating: review?.serviceAttitudeRating ?? null,
          professionalAbilityRating: review?.professionalAbilityRating ?? null,
          serviceQualityRating: review?.serviceQualityRating ?? null,
          reviewContent: review?.reviewContent ?? null,
          reviewTime: review?.reviewTime ?? null
        }
      })
      .sort((x, y) => Number(y.appointmentId) - Number(x.appointmentId))
  } catch (e) {
    myReviewRows.value = []
    ElMessage.error(e.response?.data?.message || '加载评价列表失败')
  } finally {
    myReviewsLoading.value = false
  }
}

const scheduleForm = reactive({
  workDate: '',
  startTime: '',
  endDateTime: '', // 结束日期时间（支持跨天）
  availableStatus: 1
})

const scheduleRules = {
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endDateTime: [{ required: true, message: '请选择结束日期和时间', trigger: 'change' }],
  availableStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 按状态筛选预约
const pendingAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 0)
})

const confirmedAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 1)
})

const inServiceAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 2)
})

const completedAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 3)
})

const rejectedAppointments = computed(() => {
  return appointments.value.filter(apt => apt.status === 4 || apt.status === 5)
})

const getAppointmentsByDate = (date) => {
  return appointments.value.filter(
    apt => apt.appointmentDate === date
  )
}

const getSchedulesByDate = (date) => {
  return schedulesList.value.filter(
    schedule => schedule.workDate === date && schedule.availableStatus === 1
  )
}

const getAppointmentTagType = (status) => {
  const statusMap = {
    0: 'info',      // 待接单
    1: 'warning',   // 已接单（待付款）
    2: 'success',   // 服务中
    3: 'success',   // 已完成（Element Plus 不允许传空字符串）
    4: 'info',      // 已拒单/已关闭
    5: 'danger'     // 已拒单/已关闭
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待接单',
    1: '已接单',
    2: '服务中',
    3: '已完成',
    4: '已拒单/已关闭',
    5: '已拒单/已关闭'
  }
  return statusMap[status] || '未知'
}

// 支付状态类型
const getPaymentStatusType = (paymentStatus) => {
  const config = PAYMENT_STATUS_CONFIG[paymentStatus]
  return config?.type || 'info'
}

// 支付状态文本
const getPaymentStatusText = (paymentStatus) => {
  const config = PAYMENT_STATUS_CONFIG[paymentStatus]
  return config?.text || '未知'
}

// 状态跟踪文本（用于tooltip）
const getStatusTrackingText = (appointment) => {
  const parts = []
  parts.push(`预约状态: ${getStatusText(appointment.status)}`)
  parts.push(`支付状态: ${getPaymentStatusText(appointment.paymentStatus)}`)
  if (appointment.createTime) {
    parts.push(`创建时间: ${appointment.createTime}`)
  }
  if (appointment.startTime) {
    parts.push(`开始时间: ${appointment.startTime}`)
  }
  if (appointment.endTime) {
    parts.push(`结束时间: ${appointment.endTime}`)
  }
  return parts.join('\n')
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.length === 8 ? timeStr.slice(0, 5) : timeStr
}

const formatAppointmentTime = (appointment) => {
  if (appointment.endDatetime) {
    const start = `${appointment.appointmentDate} ${formatTime(appointment.startTime)}`
    const end = appointment.endDatetime.replace('T', ' ').slice(0, 16)
    return `${start} - ${end}`
  } else {
    return `${appointment.appointmentDate} ${formatTime(appointment.startTime)} - ${formatTime(appointment.endTime)}`
  }
}

const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 检测时间冲突
const checkConflict = () => {
  if (!scheduleForm.workDate || !scheduleForm.startTime || !scheduleForm.endDateTime) {
    conflictWarning.value = ''
    return
  }
  
  const scheduleStart = dayjs(`${scheduleForm.workDate} ${scheduleForm.startTime}`)
  const scheduleEnd = dayjs(scheduleForm.endDateTime)
  
  if (scheduleEnd.isBefore(scheduleStart)) {
    conflictWarning.value = '结束时间不能早于开始时间'
    return
  }
  
  // 检查与现有预约的冲突
  const conflicts = appointments.value.filter(apt => {
    if (apt.status === 4 || apt.status === 5) return false // 已取消或已拒绝的不算冲突
    
    const aptStart = dayjs(`${apt.appointmentDate} ${formatTime(apt.startTime)}`)
    const aptEnd = apt.endDatetime 
      ? dayjs(apt.endDatetime)
      : dayjs(`${apt.appointmentDate} ${formatTime(apt.endTime)}`)
    
    return (scheduleStart.isBefore(aptEnd) && scheduleEnd.isAfter(aptStart))
  })
  
  if (conflicts.length > 0) {
    conflictWarning.value = `警告：该时间段与 ${conflicts.length} 个预约冲突，请确认是否继续`
  } else {
    conflictWarning.value = ''
  }
}

// 监听时间变化，检测冲突
watch([() => scheduleForm.workDate, () => scheduleForm.startTime, () => scheduleForm.endDateTime], () => {
  checkConflict()
})

watch(activeMainTab, async (tab) => {
  if (tab === 'my_reviews') {
    await loadMyReviews()
  }
})

// 接单
const confirmAppointment = async (appointmentId) => {
  try {
    await ElMessageBox.confirm('确定要接单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await acceptAppointment(appointmentId)
    ElMessage.success('接单成功')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '接单失败')
    }
  }
}

// 拒绝接单
const rejectAppointment = async (appointmentId) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await rejectAppointmentAPI(appointmentId)
    ElMessage.success('已拒绝')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 开始服务
const startService = async (appointmentId) => {
  try {
    // 检查支付状态
    const appointment = appointments.value.find(apt => apt.appointmentId === appointmentId)
    if (appointment && appointment.paymentStatus !== PAYMENT_STATUS.PAID) {
      ElMessage.warning('订单未支付，无法开始服务')
      return
    }
    
    await ElMessageBox.confirm('确定要开始服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await startAppointment(appointmentId)
    ElMessage.success('服务已开始')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '操作失败'
      ElMessage.error(errorMsg)
    }
  }
}

// 完成服务
const completeService = async (appointmentId) => {
  try {
    // 检查支付状态
    const appointment = appointments.value.find(apt => apt.appointmentId === appointmentId)
    if (appointment && appointment.paymentStatus !== PAYMENT_STATUS.PAID) {
      ElMessage.warning('订单未支付，无法结束服务')
      return
    }
    
    await ElMessageBox.confirm('确定要完成服务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    await completeAppointment(appointmentId)
    ElMessage.success('服务已完成，等待客户评价')
    await loadAppointments()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || '操作失败'
      ElMessage.error(errorMsg)
    }
  }
}

const editSchedule = (schedule) => {
  editingSchedule.value = schedule
  scheduleForm.workDate = schedule.workDate
  scheduleForm.startTime = formatTime(schedule.startTime)
  // 如果结束时间在同一天，使用workDate；如果跨天，使用endDate（如果有）
  const endDate = schedule.endDate || schedule.workDate
  const endTime = formatTime(schedule.endTime)
  scheduleForm.endDateTime = `${endDate} ${endTime}`
  scheduleForm.availableStatus = schedule.availableStatus
  showAddScheduleDialog.value = true
  checkConflict()
}

const deleteScheduleItem = async (scheduleId) => {
  try {
    await ElMessageBox.confirm('确定要删除此可服务时间吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteSchedule(scheduleId)
    ElMessage.success('删除成功')
    await loadSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const saveSchedule = async () => {
  if (!scheduleFormRef.value) return
  
  await scheduleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (conflictWarning.value && conflictWarning.value.includes('冲突')) {
        try {
          await ElMessageBox.confirm(conflictWarning.value, '时间冲突警告', {
            confirmButtonText: '继续保存',
            cancelButtonText: '取消',
            type: 'warning'
          })
        } catch (error) {
          if (error === 'cancel') return
        }
      }
      
      saving.value = true
      try {
        // 解析结束日期时间
        const endDateTime = dayjs(scheduleForm.endDateTime)
        const endDate = endDateTime.format('YYYY-MM-DD')
        const endTime = endDateTime.format('HH:mm:ss')

        // 前后端约定：一条排班记录必须是同一天，不支持跨天
        if (endDate !== scheduleForm.workDate) {
          ElMessage.error('当前排班不支持跨天，请拆分为多条同一天的时间段')
          saving.value = false
          return
        }
        
        const data = {
          workDate: scheduleForm.workDate,
          startTime: scheduleForm.startTime.length === 5 ? scheduleForm.startTime + ':00' : scheduleForm.startTime,
          // 根据后端最新接口说明，只需要传 workDate + startTime + endTime + availableStatus
          endTime: endTime,
          availableStatus: scheduleForm.availableStatus
        }
        
        if (editingSchedule.value) {
          await updateSchedule(editingSchedule.value.scheduleId, data)
          ElMessage.success('更新成功')
        } else {
          await createSchedule(data)
          ElMessage.success('添加成功')
        }
        
        showAddScheduleDialog.value = false
        resetScheduleForm()
        await loadSchedules()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        saving.value = false
      }
    }
  })
}

const resetScheduleForm = () => {
  editingSchedule.value = null
  scheduleForm.workDate = ''
  scheduleForm.startTime = ''
  scheduleForm.endDateTime = ''
  scheduleForm.availableStatus = 1
  conflictWarning.value = ''
}

const loadAppointments = async () => {
  if (!staffId.value) {
    console.warn('⚠️ staffId为空，无法加载预约列表')
    return
  }
  
  try {
    // 使用服务人员专用接口，不需要传递 staffId（后端会自动识别当前登录用户）
    console.log('📥 开始加载服务人员预约列表...')
    const response = await getStaffAppointmentsList()
    console.log('📥 后端返回的原始数据:', response.data)
    
    const data = response.data?.data || response.data || []
    const appointmentList = Array.isArray(data) ? data : []
    
    console.log(`✅ 成功加载 ${appointmentList.length} 条预约数据`)
    if (appointmentList.length > 0) {
      console.log('预约数据示例:', appointmentList[0])
    }
    
    await enrichAppointmentNames(appointmentList)
    appointments.value = appointmentList
  } catch (error) {
    console.error('❌ 加载预约列表失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
    appointments.value = []
    ElMessage.error(error.response?.data?.message || '加载预约列表失败')
  }
}

const viewAppointmentDetail = async (appointmentId) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  appointmentDetail.value = null
  try {
    const res = await getStaffAppointmentDetail(appointmentId)
    const detail = res.data?.data || res.data || null
    if (detail) {
      // 先补齐姓名等信息
      await enrichAppointmentNames([detail])
      appointmentDetail.value = detail
      
      // 如果支付状态为"待支付"或"未知"，主动查询支付状态以触发后端自动同步
      const paymentStatus = detail.paymentStatus !== undefined && detail.paymentStatus !== null
        ? (typeof detail.paymentStatus === 'string' ? parseInt(detail.paymentStatus, 10) : detail.paymentStatus)
        : (detail.payment_status !== undefined && detail.payment_status !== null
          ? (typeof detail.payment_status === 'string' ? parseInt(detail.payment_status, 10) : detail.payment_status)
          : 0)
      
      // 检查是否为待支付状态：0、null、undefined、'unpaid'、'0' 或 NaN
      const isUnpaid = paymentStatus === 0 || 
                       paymentStatus === null || 
                       paymentStatus === undefined || 
                       paymentStatus === 'unpaid' || 
                       paymentStatus === '0' ||
                       (typeof paymentStatus === 'string' && paymentStatus.trim() === '') ||
                       (typeof paymentStatus === 'number' && isNaN(paymentStatus))
      
      if (isUnpaid && appointmentId) {
        try {
          // 调用支付状态查询接口，触发后端自动同步支付宝状态
          await queryPaymentStatusByAppointment(appointmentId)
          // 同步后重新查询预约详情以获取最新状态
          const refreshRes = await getStaffAppointmentDetail(appointmentId)
          const refreshDetail = refreshRes.data?.data || refreshRes.data || null
          if (refreshDetail) {
            // 重新补齐姓名等信息
            await enrichAppointmentNames([refreshDetail])
            const newPaymentStatus = refreshDetail.paymentStatus !== undefined && refreshDetail.paymentStatus !== null
              ? (typeof refreshDetail.paymentStatus === 'string' ? parseInt(refreshDetail.paymentStatus, 10) : refreshDetail.paymentStatus)
              : (refreshDetail.payment_status !== undefined && refreshDetail.payment_status !== null
                ? (typeof refreshDetail.payment_status === 'string' ? parseInt(refreshDetail.payment_status, 10) : refreshDetail.payment_status)
                : 0)
            
            // 更新支付状态
            appointmentDetail.value.paymentStatus = newPaymentStatus
            appointmentDetail.value.payment_status = newPaymentStatus
            
            // 如果支付状态已更新为已支付，提示用户
            if (newPaymentStatus === 1 && paymentStatus !== 1) {
              ElMessage.success('支付状态已更新：订单已支付')
            }
          }
        } catch (error) {
          // 静默处理错误，不影响页面正常显示
          console.debug('自动同步支付状态失败:', error)
        }
      }
    } else {
      appointmentDetail.value = { appointmentId }
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '获取预约详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const loadSchedules = async () => {
  if (!staffId.value) return
  
  try {
    // 使用服务人员专用接口，不需要传递 staffId（后端会自动识别当前登录用户）
    // 可以传递日期范围等筛选参数
    const response = await getSchedules()
    const data = response.data?.data || response.data || []
    schedulesList.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载可服务时间列表失败:', error)
    schedulesList.value = []
    ElMessage.error(error.response?.data?.message || '加载可服务时间列表失败')
  }
}

onMounted(async () => {
  await Promise.all([
    loadAppointments(),
    loadSchedules()
  ])
})
</script>

<style scoped lang="scss">
.my-schedule-view {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.calendar-day {
  height: 100%;
  padding: 5px;
  
  .day-number {
    font-weight: bold;
    margin-bottom: 5px;
    font-size: 14px;
  }
  
  .day-events {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .el-tag {
      font-size: 11px;
      padding: 2px 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.schedules-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
}

.reviews-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
    }
  }
}
</style>

// components/privacy-modal/privacy-modal.js
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    show: {
      type: Boolean,
      value: false
    },
    privacyContractName: {
      type: String,
      value: '《用户隐私保护指引》'
    },
    eventInfo: {
      type: Object,
      value: {}
    }
  },

  /**
   * 组件的初始数据
   */
  data: {

  },

  /**
   * 组件的方法列表
   */
  methods: {
    // 查看隐私协议
    handleOpenPrivacyContract() {
      const app = getApp();
      app.openPrivacyContract();
    },

    // 同意隐私协议
    handleAgreePrivacyAuthorization() {
      const app = getApp();
      app.handlePrivacyAgree('privacy-agree-btn');
      
      // 隐藏弹窗
      this.setData({ show: false });
      
      // 触发同意事件
      this.triggerEvent('agree', {
        eventInfo: this.data.eventInfo
      });
    },

    // 拒绝隐私协议
    handleDisagreePrivacyAuthorization() {
      const app = getApp();
      app.handlePrivacyDisagree();
      
      // 隐藏弹窗
      this.setData({ show: false });
      
      // 触发拒绝事件
      this.triggerEvent('disagree', {
        eventInfo: this.data.eventInfo
      });
    },

    // 阻止弹窗背景点击
    preventTap() {
      // 空方法，阻止事件冒泡
    }
  }
});
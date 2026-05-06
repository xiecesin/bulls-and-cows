import api from './index';

/**
 * 游戏记录 API
 */
export const recordApi = {
  /**
   * 保存练习记录
   */
  saveRecord(data) {
    return api.post('/record/save', data);
  },

  /**
   * 获取用户的练习记录
   */
  getRecords(page = 0, size = 10) {
    return api.get('/record/list', { params: { page, size } });
  },

  /**
   * 获取用户统计数据
   */
  getStats() {
    return api.get('/record/stats');
  }
};

/**
 * 排行榜 API
 */
export const rankingApi = {
  /**
   * 获取排行榜
   */
  getRanking(type = 'GUESS_COUNT', limit = 20) {
    return api.get('/ranking', { params: { type, limit } });
  },

  /**
   * 获取我的排名
   */
  getMyRanking(type = 'GUESS_COUNT') {
    return api.get('/ranking/me', { params: { type } });
  }
};

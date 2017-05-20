热门新闻（PopularMovies）
===
一个用于浏览最热门或最高分电影的App。
## App设计
- 使用**SyncAdapter**实现定时同步。
- 提供每日推送排名第一电影的功能。
- 支持平板布局。
- 使用**SQLite**存储数据，离线时亦可浏览。
## 数据源
数据来源于[The Movie Database](https://www.themoviedb.org/)
## 依赖库
- [butterknife](https://github.com/JakeWharton/butterknife)
- [eventbus](https://github.com/greenrobot/EventBus)
## 注意
因版权原因，本项目不提供api key。
若运行时遇到以下错误：

    Error:(150, 67) 错误: 找不到符号
    符号:   变量 OPEN_MOVIE_API_KEY
    位置: 类 BuildConfig

只需于[The Movie Database](https://www.themoviedb.org/)申请API_KEY，并替换以上变量即可。

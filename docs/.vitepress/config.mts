import { defineConfig } from 'vitepress'
import { withMermaid } from 'vitepress-plugin-mermaid'

const GITHUB = 'https://github.com/xiangganLuo/dynamic-log'

// https://vitepress.dev/reference/site-config
export default withMermaid(
  defineConfig({
    lang: 'zh-CN',
    title: 'Dynamic Log',
    description: '运行期动态刷新日志级别的 Java 框架',
    lastUpdated: true,
    cleanUrls: true,
    // README.md 仅作开发说明，不作为站点页面构建
    srcExclude: ['README.md'],
    // 部署到自定义域名/根路径时保持默认 '/'；GitHub Pages 项目页可改为 '/dynamic-log/'
    base: '/',

    head: [
      ['link', { rel: 'icon', href: '/logo.svg' }],
      ['meta', { name: 'theme-color', content: '#2563eb' }],
      // 首帧前标记，用于首页动画的初始隐藏态；prefers-reduced-motion 下不添加（降级为静态）
      [
        'script',
        {},
        "try{if(!matchMedia('(prefers-reduced-motion: reduce)').matches){document.documentElement.classList.add('dl-anim')}}catch(e){}",
      ],
    ],

    themeConfig: {
      logo: '/logo.svg',
      siteTitle: 'Dynamic Log',

      nav: [
        { text: '指南', link: '/guide/introduction', activeMatch: '/guide/' },
        { text: '快速开始', link: '/guide/quickstart' },
        { text: '配置中心接入', link: '/guide/refresh' },
        { text: '官方插件', link: '/guide/plugins-official' },
        {
          text: '社区',
          items: [
            { text: '贡献指南', link: '/guide/contributing' },
            { text: '联系作者', link: '/guide/contact' },
          ],
        },
        {
          text: 'v1.0.0',
          items: [
            { text: '更新日志', link: `${GITHUB}/releases` },
            { text: '提交 Issue', link: `${GITHUB}/issues` },
          ],
        },
      ],

      sidebar: {
        '/guide/': [
          {
            text: '开始',
            items: [
              { text: '简介', link: '/guide/introduction' },
              { text: '快速开始', link: '/guide/quickstart' },
            ],
          },
          {
            text: '核心概念',
            items: [
              { text: '核心概念', link: '/guide/concepts' },
              { text: '日志系统适配器', link: '/guide/adapter' },
              { text: '事件体系', link: '/guide/events' },
              { text: '插件系统（Plugin SPI）', link: '/guide/plugin' },
            ],
          },
          {
            text: '配置中心接入',
            items: [
              { text: '动态刷新与配置中心', link: '/guide/refresh' },
            ],
          },
          {
            text: '官方插件',
            items: [
              { text: '官方模块与插件', link: '/guide/plugins-official' },
            ],
          },
          {
            text: '集成',
            items: [
              { text: 'Spring Boot 接入', link: '/guide/springboot' },
            ],
          },
          {
            text: '参考',
            items: [
              { text: '术语表', link: '/guide/glossary' },
            ],
          },
          {
            text: '社区',
            items: [
              { text: '贡献指南', link: '/guide/contributing' },
              { text: '联系作者', link: '/guide/contact' },
            ],
          },
        ],
      },

      socialLinks: [{ icon: 'github', link: GITHUB }],

      search: {
        provider: 'local',
        options: {
          translations: {
            button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
            modal: {
              noResultsText: '无法找到相关结果',
              resetButtonTitle: '清除查询条件',
              footer: {
                selectText: '选择',
                navigateText: '切换',
                closeText: '关闭',
              },
            },
          },
        },
      },

      footer: {
        message: '基于 Apache License 2.0 发布',
        copyright: `Copyright © 2024-${new Date().getFullYear()} Dynamic Log`,
      },

      outline: { level: [2, 3], label: '本页目录' },
      docFooter: { prev: '上一页', next: '下一页' },
      returnToTopLabel: '回到顶部',
      sidebarMenuLabel: '菜单',
      darkModeSwitchLabel: '主题',
      lightModeSwitchTitle: '切换到浅色模式',
      darkModeSwitchTitle: '切换到深色模式',
      externalLinkIcon: true,
    },

    // 中文分词友好的锚点
    markdown: {
      lineNumbers: false,
    },

    // mermaid 主题（跟随亮/暗自动切换由插件处理）
    mermaid: {
      theme: 'default',
    },
  })
)

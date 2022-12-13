<template>

    <div>
        <h1>123111112</h1>
    </div>
<!--    <div class="m-table-wrap">-->
<!--        <table>-->
<!--            <thead>-->
<!--            <tr>-->
<!--                <th :width="item.width" v-for="(item, index) in columns" :key="index">-->
<!--                    {{ item.title }}-->
<!--                </th>-->
<!--            </tr>-->
<!--            </thead>-->
<!--            <tbody class="m-body">-->
<!--&lt;!&ndash;            <tr v-show="loading">&ndash;&gt;-->
<!--&lt;!&ndash;                <Spin class="m-loading" :colspan="columns.length" />&ndash;&gt;-->
<!--&lt;!&ndash;            </tr>&ndash;&gt;-->
<!--            <tr v-show="!total">-->
<!--                <td class="m-empty" :colspan="columns.length">-->
<!--                    <svg class="u-empty-icon" viewBox="0 0 64 41" xmlns="http://www.w3.org/2000/svg">-->
<!--                        <g transform="translate(0 1)" fill="none" fillRule="evenodd">-->
<!--                            <ellipse fill="#F5F5F5" cx="32" cy="33" rx="32" ry="7"></ellipse>-->
<!--                            <g fillRule="nonzero" stroke="#D9D9D9">-->
<!--                                <path d="M55 12.76L44.854 1.258C44.367.474 43.656 0 42.907 0H21.093c-.749 0-1.46.474-1.947 1.257L9 12.761V22h46v-9.24z"></path>-->
<!--                                <path-->
<!--                                        d="M41.613 15.931c0-1.605.994-2.93 2.227-2.931H55v18.137C55 33.26 53.68 35 52.05 35h-40.1C10.32 35 9 33.259 9 31.137V13h11.16c1.233 0 2.227 1.323 2.227 2.928v.022c0 1.605 1.005 2.901 2.237 2.901h14.752c1.232 0 2.237-1.308 2.237-2.913v-.007z"-->
<!--                                        fill="#FAFAFA"-->
<!--                                ></path>-->
<!--                            </g>-->
<!--                        </g>-->
<!--                    </svg>-->
<!--                    <p class="u-empty-desc">暂无数据</p>-->
<!--                </td>-->
<!--            </tr>-->
<!--            <tr v-for="(data, index) in dataSource" :key="index">-->
<!--                <td v-for="(col, ind) in columns" :key="ind" :title="data[col.dataIndex]">-->
<!--                    <slot v-if="col.slot" v-bind:data="data" :name="col.slot">{{ data[col.dataIndex] || '&#45;&#45;' }}</slot>-->
<!--                    <span v-else>{{ data[col.dataIndex] || '&#45;&#45;' }}</span>-->
<!--                </td>-->
<!--            </tr>-->
<!--            </tbody>-->
<!--        </table>-->
<!--&lt;!&ndash;        <Pagination&ndash;&gt;-->
<!--&lt;!&ndash;                @change="changePage"&ndash;&gt;-->
<!--&lt;!&ndash;                :current="pagination.p"&ndash;&gt;-->
<!--&lt;!&ndash;                :pageSize="pagination.pageSize"&ndash;&gt;-->
<!--&lt;!&ndash;                :total="total"&ndash;&gt;-->
<!--&lt;!&ndash;                :hideOnSinglePage="hideOnSinglePage"&ndash;&gt;-->
<!--&lt;!&ndash;                :showQuickJumper="true"&ndash;&gt;-->
<!--&lt;!&ndash;                :showTotal="true"&ndash;&gt;-->
<!--&lt;!&ndash;                placement="right"&ndash;&gt;-->
<!--&lt;!&ndash;                v-if="pagination && total" />&ndash;&gt;-->
<!--    </div>-->
</template>


<script>
    // import Pagination from 'Pagination'
    // import Spin from 'Spin'
    module.exports = {
        name: 'table1',
        // components: {
        //     Pagination,
        //     Spin
        // },
        props: {
            columns: { // 表格列的配置项
                type: Array,
                default: () => {
                    return []
                }
            },
            dataSource: { // 表格数据数组
                type: Array,
                default: () => {
                    return []
                }
            },
            // pagination: { // 分页器，为false时不展示和进行分页
            //     type: [Boolean, Object],
            //     default: false
            // },
            hideOnSinglePage: { // 只有一页时是否隐藏分页器
                type: Boolean,
                default: false
            },
            total: { // 数据总数
                type: Number,
                default: 0
            },
            loading: { // 页面是否加载中
                type: Boolean,
                default: false
            }
        }
        // methods: {
        //     changePage (pager) { // 分页器回调
        //         this.$emit('change', pager)
        //     }
        // }
    }
</script>
<style lang="less" scoped>
    .m-table-wrap {
        color: rgba(0, 0, 0, 0.65);
        font-size: 14px;
        line-height: 1.5;
        table {
            table-layout: fixed;
            width: 100%;
            text-align: left;
            border-radius: 4px 4px 0 0;
            border-collapse: separate;
            border-spacing: 0;
            thead tr th {
                padding: 16px;
                color: rgba(0, 0, 0, 0.85);
                font-weight: 500;
                text-align: left;
                background: #fafafa;
                border-bottom: 1px solid #e8e8e8;
                transition: background .3s ease;
                &:first-child {
                    border-top-left-radius: 4px;
                }
                &:last-child {
                    border-top-right-radius: 4px;
                }
            }
            .m-body {
                position: relative;
                .m-loading {
                    position: absolute;
                    width: 100%;
                    height: 100%;
                }
                .m-empty {
                    padding: 48px 16px;
                    color: rgba(0, 0, 0, 0.25);
                    font-size: 14px;
                    text-align: center;
                    background: #fff;
                    border-bottom: 1px solid #e8e8e8;
                    border-radius: 0 0 2px 2px;
                    .u-empty-icon {
                        width: 64px;
                        height: 41px;
                        margin-bottom: 8px;
                    }
                    .u-empty-desc {
                        color: rgba(0, 0, 0, 0.25);
                        font-size: 14px;
                    }
                }
            }
            tbody tr {
                transition: background .3s;
                td {
                    padding: 16px;
                    border-bottom: 1px solid #e8e8e8;
                    transition: background .3s;
                    overflow: hidden;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                }
                &:hover {
                    background: rgba(64, 169, 255, 0.1);
                }
            }
        }
    }
</style>
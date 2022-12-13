<template>
    <div class="m-spin-wrap">
        <div class="m-spin" v-show="spinning">
            <div class="m-spin-box">
                <div class="m-spin-dot">
                    <span class="u-dot-item"></span>
                    <span class="u-dot-item"></span>
                    <span class="u-dot-item"></span>
                    <span class="u-dot-item"></span>
                </div>
                <p class="u-tip" v-show="tip">{{ tip }}</p>
            </div>
        </div>
        <div :class="['m-spin-content', {'m-spin-mask': spinning}]">
            <slot></slot>
        </div>
    </div>
</template>
<script>
    module.exports = {
        name: 'Spin',
        props: {
            spinning: { // 是否为加载中状态
                type: Boolean,
                default: true
            },
            tip: { // 描述文案
                type: String,
                default: ''
            }
        }
    }
</script>
<style lang="less" scoped>
    @themeColor: #1890ff;
    .m-spin-wrap {
        position: relative;
        height: 100%;
    }
    .m-spin {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        z-index: 9;
        .m-spin-box {
            text-align: center;
            .m-spin-dot {
                position: relative;
                display: inline-block;
                width: 36px;
                height: 36px;
                transform: rotate(45deg);
                -ms-transform: rotate(45deg); /* Internet Explorer */
                -moz-transform: rotate(45deg); /* Firefox */
                -webkit-transform: rotate(45deg); /* Safari 和 Chrome */
                -o-transform: rotate(45deg); /* Opera */
                animation: rotate 1.2s linear infinite;
                -webkit-animation: rotate 1.2s linear infinite;
                @keyframes rotate {
                    100% {transform: rotate(405deg);}
                }
                .u-dot-item { // 单个圆点样式
                    position: absolute;
                    width: 12px;
                    height: 12px;
                    background: @themeColor;
                    border-radius: 50%;
                    opacity: .3;
                    animation: spinMove 1s linear infinite alternate;
                    -webkit-animation: spinMove 1s linear infinite alternate;
                    @keyframes spinMove {
                        100% {opacity: 1;}
                    }
                }
                .u-dot-item:first-child {
                    top: 0;
                    left: 0;
                }
                .u-dot-item:nth-child(2) {
                    top: 0;
                    right: 0;
                    animation-delay: .4s;
                    -webkit-animation-delay: .4s;
                }
                .u-dot-item:nth-child(3) {
                    bottom: 0;
                    right: 0;
                    animation-delay: .8s;
                    -webkit-animation-delay: .8s;
                }
                .u-dot-item:last-child {
                    bottom: 0;
                    left: 0;
                    animation-delay: 1.2s;
                    -webkit-animation-delay: 1.2s;
                }
            }
            .u-tip {
                font-size: 16px;
                font-weight: 500;
                line-height: 20px;
                color: @themeColor;
                text-align: center;
                padding-top: 10px;
            }
        }
    }
    .m-spin-content {
        position: absolute;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
        z-index: 8;
        transition: all .3s;
    }
    .m-spin-mask {
        opacity: .5;
        background: rgba(255, 255, 255, .4);
    }
</style>
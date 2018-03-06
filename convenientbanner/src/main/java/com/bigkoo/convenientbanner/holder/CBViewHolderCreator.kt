package com.bigkoo.convenientbanner.holder

/**
 * @ClassName :  ViewHolderCreator
 * @Description :
 * @Author Sai
 * @Date 2014年11月30日 下午3:29:34
 */
interface CBViewHolderCreator<Holder> {
    fun createHolder(): Holder
}

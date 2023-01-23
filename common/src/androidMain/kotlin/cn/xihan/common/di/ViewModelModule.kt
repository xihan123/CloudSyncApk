package cn.xihan.common.di

import cn.xihan.common.ui.update.UpdateViewModel
import org.koin.dsl.module

val androidModule = module {
    single { UpdateViewModel() }
}
package com.example.filesystem.data

import android.content.res.Resources
import com.example.filesystem.R

/* Returns initial list of myFiles. */
fun myFilesList(resources: Resources): List<MyFile> {
    return listOf(
        MyFile(
            id = 1,
            name = resources.getString(R.string.myfile1_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile1_description)
        ),
        MyFile(
            id = 2,
            name = resources.getString(R.string.myfile2_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile2_description)
        ),
        MyFile(
            id = 3,
            name = resources.getString(R.string.myfile3_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile3_description)
        ),
        MyFile(
            id = 4,
            name = resources.getString(R.string.myfile4_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile4_description)
        ),
        MyFile(
            id = 5,
            name = resources.getString(R.string.myfile5_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile5_description)
        ),
        MyFile(
            id = 6,
            name = resources.getString(R.string.myfile6_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile6_description)
        ),
        MyFile(
            id = 7,
            name = resources.getString(R.string.myfile7_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile7_description)
        ),
        MyFile(
            id = 8,
            name = resources.getString(R.string.myfile8_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile8_description)
        ),
        MyFile(
            id = 9,
            name = resources.getString(R.string.myfile9_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile9_description)
        ),
        MyFile(
            id = 10,
            name = resources.getString(R.string.myfile10_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile10_description)
        ),
        MyFile(
            id = 11,
            name = resources.getString(R.string.myfile11_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile11_description)
        )
    )
}
package com.example.filesystem.data

import android.content.res.Resources
import com.example.filesystem.R

/* Returns initial list of sanFiles. */
fun sanFilesList(resources: Resources): List<SanFile> {
    return listOf(
        SanFile(
            id = 1,
            name = resources.getString(R.string.myfile1_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile1_description)
        ),
        SanFile(
            id = 2,
            name = resources.getString(R.string.myfile2_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile2_description)
        ),
        SanFile(
            id = 3,
            name = resources.getString(R.string.myfile3_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile3_description)
        ),
        SanFile(
            id = 4,
            name = resources.getString(R.string.myfile4_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile4_description)
        ),
        SanFile(
            id = 5,
            name = resources.getString(R.string.myfile5_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile5_description)
        ),
        SanFile(
            id = 6,
            name = resources.getString(R.string.myfile6_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile6_description)
        ),
        SanFile(
            id = 7,
            name = resources.getString(R.string.myfile7_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile7_description)
        ),
        SanFile(
            id = 8,
            name = resources.getString(R.string.myfile8_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile8_description)
        ),
        SanFile(
            id = 9,
            name = resources.getString(R.string.myfile9_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile9_description)
        ),
        SanFile(
            id = 10,
            name = resources.getString(R.string.myfile10_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile10_description)
        ),
        SanFile(
            id = 11,
            name = resources.getString(R.string.myfile11_name),
            image = androidx.transition.R.drawable.abc_btn_default_mtrl_shape,
            description = resources.getString(R.string.myfile11_description)
        )
    )
}
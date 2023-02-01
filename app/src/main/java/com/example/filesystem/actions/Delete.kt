package com.example.filesystem.actions

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.Selection
import com.example.filesystem.FolderFragment
import com.example.filesystem.MyDialogFragment
import com.example.filesystem.R
import com.example.filesystem.Utils
import com.example.filesystem.databinding.FragmentFolderBinding

//class Test(activity: FragmentActivity) {
//    val mActivity = activity
//    fun yes() {
//        Toast.makeText(mActivity, "yes", Toast.LENGTH_SHORT).show()
//    }
//    fun no() {
//        Toast.makeText(mActivity, "no", Toast.LENGTH_SHORT).show()
//    }
//}

interface DummyDialogCallback {
    fun onDummyDialogClick()
}


class Delete(fragment: FolderFragment) : DummyDialogCallback {
    private val mFragment = fragment
    private lateinit var mBinding : FragmentFolderBinding
    private lateinit var mSelection : Selection<String>
    private lateinit var mFinish : (Boolean) -> Unit

    fun handle(activity: FragmentActivity,
               binding: FragmentFolderBinding,
               selection: Selection<String>,
               fragmentUri: Uri,
               finish: ((Boolean) -> Unit)) {

        mBinding = binding
        mSelection = selection
        mFinish = finish

        if (!validate()) {
            return
        }

        val docId = mSelection.toList()[0]
        val uri = DocumentsContract.buildDocumentUriUsingTree(fragmentUri, docId)
        Handler().post {
            if (!mFragment.requireActivity().isFinishing) {
                /*
                val (builder, dialog) = mFragment.getAlertDialog()
                builder.setMessage("Are you sure?")
                builder.setPositiveButton("Yes") { _, _ ->
                    DocumentsContract.deleteDocument(activity.contentResolver, uri)
                    Utils.withDelay({ binding.toggleGroup.uncheck(R.id.action_delete) })
                    finish(true)
                }
                builder.setNegativeButton("No") { _, _ ->
                    Utils.withDelay({ binding.toggleGroup.uncheck(R.id.action_delete) })
                    finish(false)
                }
                builder.show()
                //dialog.dismiss()

                 */
//                val dialog = MyDialogFragment()
//                val yes = {  }
//                val no = { Toast.makeText(activity, "no", Toast.LENGTH_SHORT).show() }
//                val bundle = Bundle()
//                val test = Test(activity)
//                fun sum() = 1 + 2
//                bundle.putSerializable("test", sum())
//                dialog.show(activity.supportFragmentManager, "sdfsdf")

                fun yes() {
                    Toast.makeText(activity, "yes", Toast.LENGTH_SHORT).show()
                }
                fun no() {
                    Toast.makeText(activity, "no", Toast.LENGTH_SHORT).show()
                }

                // https://lukeneedham.medium.com/listeners-in-dialogfragments-be636bd7f480
                // https://stackoverflow.com/questions/64869501/how-to-replace-settargetfragment-now-that-it-is-deprecated
//                class MyDialogFragment : DialogFragment() {
//                    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//                        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
//                        //arguments.
//                        //private val mBuilder: AlertDialog.Builder = builder
//                        builder.setTitle("Really?")
//                        builder.setMessage("Are you sure?")
//                        //null should be your on click listener
//                        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->  })
//                        builder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
//                        return builder.create()
//                    }
//                }

                val dialog = MyDialogFragment()
                dialog.setTargetFragment(mFragment, 1)
                dialog.show(mFragment.requireFragmentManager(), "sdfsdf")

                //val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                //val dialog = MyDialogFragment(builder)
                //dialog.show(activity.supportFragmentManager, "sdfsdf")
            }
        }
    }

    private fun validate() : Boolean {
        if (mSelection.size() == 0) {
            Utils.showPopup(mFragment, "Select a file to delete") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
                mFinish(false)
            }
            return false
        }
        if (mSelection.size() > 1) {
            Utils.showPopup(mFragment, "Multi-file delete is not supported") {
                mBinding.toggleGroup.uncheck(R.id.action_delete)
                mFinish(false)
            }
            return false
        }
        return true
    }

    override fun onDummyDialogClick() {
        Toast.makeText(mFragment.requireActivity(), "Dummy click", Toast.LENGTH_SHORT).show()
    }
}
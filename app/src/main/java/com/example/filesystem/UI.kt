package com.example.filesystem

import com.example.filesystem.databinding.FragmentFolderBinding

class UI {
    companion object {
        fun handleActiveCopy(receiver: MainReceiver, binding: FragmentFolderBinding) {
            if (receiver.getActionState("copy", "sourceUri") != null) {
                binding.toggleGroup.check(R.id.action_copy)
                val sourceFragmentDocId = receiver.getActionState("copy", "sourceFragmentDocId")!!
                val sourceDocId = receiver.getActionState("copy", "sourceDocId")!!
                Utils.showStatus(binding.status, "Copying", sourceFragmentDocId, sourceDocId)
            } else {
                binding.toggleGroup.uncheck(R.id.action_copy)
            }
        }

        fun handleActiveMove(receiver: MainReceiver, binding: FragmentFolderBinding) {
            if (receiver.getActionState("move", "sourceUri") != null) {
                binding.toggleGroup.check(R.id.action_move)
                val sourceFragmentDocId = receiver.getActionState("move", "sourceFragmentDocId")!!
                val sourceDocId = receiver.getActionState("move", "sourceDocId")!!
                Utils.showStatus(binding.status, "Moving", sourceFragmentDocId, sourceDocId)
            } else {
                binding.toggleGroup.uncheck(R.id.action_move)
            }
        }
    }
}
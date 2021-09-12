package fi.tuni.sinipelto.disqs.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import fi.tuni.sinipelto.disqs.model.Comment
import fi.tuni.sinipelto.disqs.model.Post
import java.util.*

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()

    private val _posts = MutableLiveData<LinkedList<Post>>()
    val posts: LiveData<LinkedList<Post>> = Transformations.map(_posts) { it }

    private val _comments = MutableLiveData<LinkedList<Comment>>()
    val comments: LiveData<List<Comment>> = Transformations.map(_comments) { it }

    init {
        _posts.value = LinkedList()
        _comments.value = LinkedList()
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun loadPosts(data: List<Post>) {
        _posts.postValue(LinkedList(data))
    }

    fun insertPost(post: Post) {
        _posts.value!!.push(post)
    }

    fun deletePost(post: Post) {
        _posts.value!!.remove(post)
    }

    fun loadComments(data: List<Comment>) {
        _comments.postValue(LinkedList(data))
    }

    fun insertComment(comment: Comment) {
        _comments.value!!.push(comment)
    }

    companion object {
//        private const val TAG = "disqs.PageViewModel"
    }
}
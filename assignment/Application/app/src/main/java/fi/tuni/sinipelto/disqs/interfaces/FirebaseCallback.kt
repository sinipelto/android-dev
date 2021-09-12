package fi.tuni.sinipelto.disqs.interfaces

interface FirebaseCallback<T> {

    fun onResult(result: T?)

    fun onError(t: Throwable)

}
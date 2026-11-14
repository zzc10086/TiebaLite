package com.huanchengfly.tieba.post.ui.utils

import com.huanchengfly.tieba.post.api.models.protos.Media
import com.huanchengfly.tieba.post.api.models.protos.Post
import com.huanchengfly.tieba.post.api.models.protos.ThreadInfo
import com.huanchengfly.tieba.post.models.LoadPicPageData
import com.huanchengfly.tieba.post.models.PhotoViewData
import com.huanchengfly.tieba.post.models.PicItem
import com.huanchengfly.tieba.post.ui.common.PbContentRender
import com.huanchengfly.tieba.post.ui.common.PicContentRender
import com.huanchengfly.tieba.post.utils.ImageUtil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun getPhotoViewData(
    forumId: Long,
    forumName: String,
    threadId: Long,
    postId: Long,
    picId: String,
    picUrl: String,
    originUrl: String,
    showOriginBtn: Boolean,
    originSize: Int,
    seeLz: Boolean = false
): PhotoViewData {
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forumId,
            forumName = forumName,
            threadId = threadId,
            postId = postId,
            objType = "pb",
            picId = picId,
            picIndex = 1,
            seeLz = seeLz,
            originUrl = originUrl,
        ),
        picItems = persistentListOf(
            PicItem(
                picId = picId,
                picIndex = 1,
                url = picUrl,
                originUrl = originUrl,
                showOriginBtn = showOriginBtn,
                originSize = originSize,
                postId = postId
            )
        )
    )
}

fun getPhotoViewData(
    post: Post,
    picId: String,
    picUrl: String,
    originUrl: String,
    showOriginBtn: Boolean,
    originSize: Int,
    seeLz: Boolean = false
): PhotoViewData? {
    val forum = post.from_forum ?: return null
    return getPhotoViewData(
        forumId = forum.id,
        forumName = forum.name,
        threadId = post.tid,
        postId = post.id,
        picId = picId,
        picUrl = picUrl,
        originUrl = originUrl,
        showOriginBtn = showOriginBtn,
        originSize = originSize,
        seeLz = seeLz
    )
}

/**
 * 为渲染列表中的图片补齐 [PhotoViewData]。
 *
 * 部分数据来源（如 pb/floor 响应）没有携带 forum/thread 上下文，
 * 导致其中的 [PicContentRender] 无法打开全屏看图；这里使用页面已知的
 * forum/thread 信息补齐，已有的 [PhotoViewData] 不会被覆盖。
 *
 * @return 上下文未就绪（forumId 或 threadId 为 0）时返回原列表。
 */
fun ImmutableList<PbContentRender>.withPhotoViewData(
    forumId: Long,
    forumName: String,
    threadId: Long,
    postId: Long,
    seeLz: Boolean = false,
): ImmutableList<PbContentRender> {
    if (forumId == 0L || threadId == 0L) return this
    return map { render ->
        if (render is PicContentRender && render.photoViewData == null) {
            render.copy(
                photoViewData = getPhotoViewData(
                    forumId = forumId,
                    forumName = forumName,
                    threadId = threadId,
                    postId = postId,
                    picId = render.picId,
                    picUrl = render.picUrl,
                    originUrl = render.originUrl,
                    showOriginBtn = render.showOriginBtn,
                    originSize = render.originSize,
                    seeLz = seeLz
                )
            )
        } else {
            render
        }
    }.toImmutableList()
}

fun getPhotoViewData(
    threadInfo: ThreadInfo,
    index: Int
): PhotoViewData {
    return getPhotoViewData(
        medias = threadInfo.media,
        forumId = threadInfo.forumId,
        forumName = threadInfo.forumName,
        threadId = threadInfo.threadId,
        index = index
    )
}

fun getPhotoViewData(
    medias: List<Media>,
    forumId: Long,
    forumName: String,
    threadId: Long,
    index: Int
): PhotoViewData {
    val media = medias[index]
    return PhotoViewData(
        data = LoadPicPageData(
            forumId = forumId,
            forumName = forumName,
            threadId = threadId,
            postId = media.postId,
            seeLz = false,
            objType = "index",
            picId = ImageUtil.getPicId(media.originPic),
            picIndex = index + 1,
            originUrl = media.originPic
        ),
        picItems = medias.mapIndexed { mediaIndex, mediaItem ->
            PicItem(
                picId = ImageUtil.getPicId(mediaItem.originPic),
                picIndex = mediaIndex + 1,
                url = mediaItem.bigPic,
                originUrl = mediaItem.originPic,
                showOriginBtn = mediaItem.showOriginalBtn == 1,
                originSize = mediaItem.originSize,
                postId = mediaItem.postId
            )
        }.toImmutableList(),
        index = index
    )
}
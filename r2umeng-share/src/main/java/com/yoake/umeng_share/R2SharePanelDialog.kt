package com.yoake.umeng_share

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.umeng.socialize.ShareAction
import com.umeng.socialize.bean.SHARE_MEDIA
import com.umeng.socialize.media.UMImage
import com.umeng.socialize.media.UMWeb
import com.yoake.r2base.helper.R2DrawableHelper
import com.yoake.r2base.kit.bool
import com.yoake.r2base.kit.dimen
import com.yoake.r2base.kit.integer
import com.yoake.r2base.kit.onClick
import com.yoake.r2base.kit.toast
import com.yoake.r2base.obj.decoration.R2GridItemDecoration
import com.yoake.r2base.obj.decoration.R2LinearItemDecoration
import com.yoake.r2base.permissions.R2PermissionLauncher
import com.yoake.r2base.utils.R2StorageUtils


class R2SharePanelDialog(private val mContext: Context) :
    BottomSheetDialog(mContext, R.style.BottomSheetDialog), OnItemClickListener {
    private var isPosterMode: Boolean = false
    private val adapter = Adapter()
    private val shareItems: MutableList<ShareItem> = ArrayList()
    private var shareInfo: ShareInfo? = null
    private lateinit var root: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var cancelView: View
    private lateinit var posterContainer: FrameLayout
    private var posterView: View? = null
    var onItemClick: ((Int, ShareItem) -> Boolean)? = null
    override fun show() {
        super.show()
        val parent = root.parent as View
        parent.layoutParams.height = -1
        val bottomSheetBehavior = BottomSheetBehavior.from<View>(parent)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        //禁止拖动
        behavior.isDraggable = false
        //隐藏时，跳过折叠状态，直接进入隐藏状态
        behavior.skipCollapsed = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = LayoutInflater.from(mContext).inflate(R.layout.layout_share_panel, null, false)
        recyclerView = root.findViewById(R.id.recycler_view)
        cancelView = root.findViewById(R.id.cancel_view)
        posterContainer = root.findViewById(R.id.poster_view)
        setContentView(root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        val isGrid = mContext.bool(R.bool.share_panel_is_grid)
        adapter.isGrid = isGrid
        val space = mContext.dimen(R.dimen.share_panel_item_space)
        if (isGrid) {
            val spanCount = mContext.integer(R.integer.share_panel_span_count)
            recyclerView.apply {
                layoutManager = GridLayoutManager(mContext, spanCount)
                addItemDecoration(R2GridItemDecoration(spanCount, 0, space, false))
            }
        } else {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
                addItemDecoration(R2LinearItemDecoration(space, space))
            }
        }
        cancelView.onClick {
            dismiss()
        }
        root.onClick {
            dismiss()
        }
        recyclerView.adapter = adapter
        adapter.onItemClick = this

    }


    override fun onItemClick(position: Int, item: ShareItem) {

        onItemClick?.let {
            if (it.invoke(position, item)) return
        }
        when (item.type) {
            ShareItem.TYPE_QQ -> {
                if (isPosterMode) {
                    shareImage(SHARE_MEDIA.QQ)
                } else {
                    shareWeb(SHARE_MEDIA.QQ)
                }
            }

            ShareItem.TYPE_QQ_ZONE -> {
                if (isPosterMode) {
                    shareImage(SHARE_MEDIA.QZONE)
                } else {
                    shareWeb(SHARE_MEDIA.QZONE)
                }
            }

            ShareItem.TYPE_WECHAT -> {
                if (isPosterMode) {
                    shareImage(SHARE_MEDIA.WEIXIN)
                } else {
                    shareWeb(SHARE_MEDIA.WEIXIN)
                }
            }

            ShareItem.TYPE_WECHAT_CIRCLE -> {
                if (isPosterMode) {
                    shareImage(SHARE_MEDIA.WEIXIN_CIRCLE)
                } else {
                    shareWeb(SHARE_MEDIA.WEIXIN_CIRCLE)
                }

            }

            ShareItem.TYPE_SINA_WEIBO -> {
                if (isPosterMode) {
                    shareImage(SHARE_MEDIA.SINA)
                } else {
                    shareWeb(SHARE_MEDIA.SINA)
                }
            }

            ShareItem.TYPE_COPY_LINK -> {
                val clipboard =
                    mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", shareInfo?.shareUrl())
                clipboard.setPrimaryClip(clip)
                toast(mContext, mContext.getString(R.string.share_panel_share_copy_tip))
                dismiss()
            }

            ShareItem.TYPE_SHARE_POSTER -> {
                val iterator = shareItems.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    //移除这些
                    when (next.type) {
                        ShareItem.TYPE_SHARE_POSTER,
                        ShareItem.TYPE_COPY_LINK,
                        ShareItem.TYPE_BOOKMARK,
                        ShareItem.TYPE_CUSTOM_1,
                        ShareItem.TYPE_CUSTOM_2,
                        ShareItem.TYPE_CUSTOM_3,
                        -> {
                            iterator.remove()
                        }

                        else -> {}
                    }
                }
                //添加下载
                shareItems.add(
                    0, ShareItem(
                        context.getString(R.string.share_panel_share_poster_download),
                        R.drawable.icon_share_poster_download,
                        ShareItem.TYPE_DOWN_POSTER
                    )
                )
                adapter.submitList(shareItems)
                posterContainer.visibility = View.VISIBLE
                isPosterMode = true
            }

            ShareItem.TYPE_DOWN_POSTER -> {

                R2PermissionLauncher().with(mContext as FragmentActivity)
                    .denied { }
                    .granted {
                        val posterBitmap =
                            R2DrawableHelper.createBitmapFromView(posterContainer)
                        R2StorageUtils.saveBitmapToPicturePublicFolder(
                            mContext, posterBitmap
                        )
                        posterBitmap?.recycle()
                        dismiss()
                    }
                    .request(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        arrayOf("存储写入授权提示"),
                        arrayOf(mContext.getString(R.string.share_panel_download_permission_tip)),
                    )
            }

        }

    }


    fun setDate(
        shareInfo: ShareInfo,
        shareItems: MutableList<ShareItem> = optDefaultShareItems(),
        posterView: View? = null
    ) {
        this.shareInfo = shareInfo
        this.posterView = posterView
        if (this.posterView == null) {
            shareItems.remove(
                ShareItem(
                    context.getString(R.string.share_panel_share_poster),
                    R.drawable.icon_share_poster,
                    ShareItem.TYPE_SHARE_POSTER
                )
            )
        } else {
            this.posterContainer.addView(posterView)
        }
        this.shareItems.clear()
        this.shareItems.addAll(shareItems)
        adapter.submitList(this.shareItems)
    }

    fun updateItem(item: ShareItem) {
        val index = adapter.items.indexOf(item)
        adapter.set(index, item)
    }

    private fun shareWeb(platform: SHARE_MEDIA) {
        shareInfo?.let {
            val web = UMWeb(it.shareUrl()).apply {
                title = it.shareTitle()
                setThumb(UMImage(mContext, it.shareThumb()))
                description = it.shareDescription()
            }

            ShareAction(mContext as Activity).withMedia(web).setPlatform(platform).share()
        }
        dismiss()
    }


    private fun shareImage(platform: SHARE_MEDIA) {
        shareInfo?.let {
            val posterBitmap = R2DrawableHelper.createBitmapFromView(posterContainer)
            val image = UMImage(mContext, posterBitmap)
            ShareAction(mContext as Activity).withMedia(image).setPlatform(platform).share()
        }
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        isPosterMode = false
        posterContainer.visibility = View.GONE
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleNameText: TextView = itemView.findViewById(R.id.title_name_text)
        val titleImageView: ImageView = itemView.findViewById(R.id.title_image_view)
    }


    class Adapter : RecyclerView.Adapter<ViewHolder>(), OnClickListener {
        var isGrid = false
         var onItemClick: OnItemClickListener? = null
        var items: MutableList<ShareItem> = ArrayList()

        fun set(@IntRange(from = 0) position: Int, data: ShareItem) {
            if (position >= items.size) {
                throw IndexOutOfBoundsException("position: ${position}. size:${items.size}")
            }
            items[position] = data
            notifyItemChanged(position)
        }

        fun submitList(list: MutableList<ShareItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            items[position].let {
                holder.titleNameText.text = it.title
                holder.titleImageView.setImageResource(it.icon)
            }
            holder.itemView.tag = position
            holder.itemView.setOnClickListener(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val root =
                LayoutInflater.from(parent.context).inflate(R.layout.item_share, parent, false)
            //非宫格模式 宽就要设置成wrap
            if (!isGrid) {
                root.layoutParams.width = -2
            }
            return ViewHolder(root)
        }

        override fun getItemCount() = items.size
        override fun onClick(v: View?) {
            val position = v?.tag as Int
            onItemClick?.onItemClick(position, items[position])
        }
    }


    private fun optDefaultShareItems(): MutableList<ShareItem> {
        return ArrayList<ShareItem>().apply {
            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_poster),
                    R.drawable.icon_share_poster,
                    ShareItem.TYPE_SHARE_POSTER
                )
            )

            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_qq),
                    R.drawable.icon_share_qq,
                    ShareItem.TYPE_QQ
                )
            )
            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_qq_zone),
                    R.drawable.icon_share_qq_zone,
                    ShareItem.TYPE_QQ_ZONE
                )
            )


            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_wechat),
                    R.drawable.icon_share_wechat,
                    ShareItem.TYPE_WECHAT
                )
            )
            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_wechat_circle),
                    R.drawable.icon_share_wechat_circle,
                    ShareItem.TYPE_WECHAT_CIRCLE
                )
            )

            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_sina_weibo),
                    R.drawable.icon_share_sina_weibo,
                    ShareItem.TYPE_SINA_WEIBO
                )
            )

            add(
                ShareItem(
                    context.getString(R.string.share_panel_share_copy),
                    R.drawable.icon_share_copy_link,
                    ShareItem.TYPE_COPY_LINK
                )
            )
        }


    }


}
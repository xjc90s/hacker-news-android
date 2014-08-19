package io.dwak.holohackernews.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.dwak.holohackernews.app.network.models.NodeHNAPIComment;
import io.dwak.holohackernews.app.network.models.NodeHNAPIStoryDetail;

/**
 * Created by vishnu on 5/4/14.
 */
public class CommentsListAdapter extends ArrayAdapter<NodeHNAPIComment> {
    private final int mResource;
    private List<NodeHNAPIComment> mNodeHNAPIComments;
    private Context mContext;
    private List<NodeHNAPIComment> mExpandedNodeHNAPIComments;
    private NodeHNAPIStoryDetail mNodeHNAPIStoryDetail;

    public CommentsListAdapter(Context context, int resource, List<NodeHNAPIComment> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        mNodeHNAPIComments = objects;
        mExpandedNodeHNAPIComments = new ArrayList<NodeHNAPIComment>();
    }

    private void expandComments(NodeHNAPIComment nodeHNAPIComment) {
        mExpandedNodeHNAPIComments.add(nodeHNAPIComment);

        if (nodeHNAPIComment.getChildNodeHNAPIComments().size() == 0) {
            return;
        }

        for (NodeHNAPIComment childNodeHNAPIComment : nodeHNAPIComment.getChildNodeHNAPIComments()) {
            expandComments(childNodeHNAPIComment);
        }

    }

    public void setNodeHNAPIStoryDetail(NodeHNAPIStoryDetail nodeHNAPIStoryDetail) {
        mNodeHNAPIStoryDetail = nodeHNAPIStoryDetail;
    }

    public void setNodeHNAPIComments(List<NodeHNAPIComment> nodeHNAPIComments) {
        mNodeHNAPIComments = nodeHNAPIComments;
        mExpandedNodeHNAPIComments.clear();
        for (NodeHNAPIComment nodeHNAPIComment : mNodeHNAPIComments) {
            expandComments(nodeHNAPIComment);
        }
    }

    @Override
    public int getCount() {
        return mExpandedNodeHNAPIComments.size();
    }

    @Override
    public NodeHNAPIComment getItem(int position) {
        return mExpandedNodeHNAPIComments.get(position);
    }

    @Override
    public int getPosition(NodeHNAPIComment item) {
        return mExpandedNodeHNAPIComments.indexOf(item);
    }

    @Override
    public int getItemViewType(int position) {
        return mExpandedNodeHNAPIComments.get(position).getLevel();
    }

    private void commentAction(final int i) {
        final CharSequence[] commentActions = {"Share Comment", "Share Comment Content"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(commentActions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                switch (j) {
                    case 0:
                        sendIntent.putExtra(Intent.EXTRA_TEXT,
                                "https://news.ycombinator.com/item?id=" + getItem(i).getId());
                        break;
                    case 1:
                        sendIntent.putExtra(Intent.EXTRA_TEXT,
                                getItem(i).getUser() + ": " + Html.fromHtml(getItem(i).getContent()));
                        break;
                }
                sendIntent.setType("text/plain");
                mContext.startActivity(sendIntent);
            }
        });

        builder.create().show();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;


        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(mResource, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.mCommentContent = (TextView) convertView.findViewById(R.id.comment_content);
            viewHolder.mColorCodeView = convertView.findViewById(R.id.color_code);
            viewHolder.mCommentSubmissionTime = (TextView) convertView.findViewById(R.id.comment_submission_time);
            viewHolder.mCommentSubmitter = (TextView) convertView.findViewById(R.id.comment_submitter);
            viewHolder.mOverflow = (ImageButton) convertView.findViewById(R.id.comment_overflow);

            convertView.setTag(viewHolder);
        }

        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Spanned commentContent = Html.fromHtml(getItem(position).getContent());
        viewHolder.mCommentContent.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.mCommentContent.setText(commentContent);
        viewHolder.mCommentSubmissionTime.setText(getItem(position).getTimeAgo());
        viewHolder.mOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentAction(position);
            }
        });

        String submitter = getItem(position).getUser();
        if (HoloHackerNewsApplication.isDebug()) {
            viewHolder.mCommentSubmitter.setText(position + " " + submitter);
        }
        else {
            viewHolder.mCommentSubmitter.setText(submitter);
        }
        viewHolder.mCommentSubmitter.setTextColor(
                mContext.getResources().getColor(
                        mNodeHNAPIStoryDetail.getUser().equals(submitter)
                                ? android.R.color.holo_orange_light
                                : android.R.color.black
                )
        );

        float scale = mContext.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (getItem(position).getLevel() * 12 * scale + 0.5f);

        if (getItem(position).getLevel() != 0) {
            convertView.setPadding(dpAsPixels, 0, 4, 0);
        }
        else {
            convertView.setPadding(4, 0, 4, 0);
        }

        switch (getItem(position).getLevel() % 8) {
            case 0:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_blue_bright);
                break;
            case 1:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_green_light);
                break;
            case 2:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_red_light);
                break;
            case 3:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_orange_light);
                break;
            case 4:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_purple);
                break;
            case 5:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_green_dark);
                break;
            case 6:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_red_dark);
                break;
            case 7:
                viewHolder.mColorCodeView.setBackgroundResource(android.R.color.holo_orange_dark);
                break;
        }
        return convertView;
    }

    static class ViewHolder {
        TextView mCommentContent;
        View mColorCodeView;
        TextView mCommentSubmitter;
        TextView mCommentSubmissionTime;
        ImageButton mOverflow;
    }
}

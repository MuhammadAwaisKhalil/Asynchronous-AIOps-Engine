import streamlit as st
import pandas as pd
import log_errors
import time


st.set_page_config('AI Self Healing Telemetry Control',layout='wide')
st.title('AI Self Healing System Telemetry Dashboard')
st.write('Monitoring System anomalies and correcting any that arise')

placeholder = st.empty()

while True:
    df = log_errors.getData()

    with placeholder.container():
        if df.empty:
            st.info('No errors logged yet')
        else:

            col1, col2, col3, col4 = st.columns(4)
            col1.metric('Total events logged',len(df))
            col2.metric('DB Deadlocks handled',len(df[df['prediction']=='DATABASE_DEADLOCK']))
            col3.metric('Security Attacks blocked',len(df[df['prediction']=='SECURITY_AUTH_BREACH']))
            col4.metric('Resource Exhaution Managed',len(df[df['prediction']=='RESOURCE_EXHAUTION']))

            st.markdown('---')    

            chart_col, table_col = st.columns([1, 1])

            with chart_col:
                st.subheader('Distribution of Incident Types')
                type_counts = df['prediction'].value_counts().reset_index()
                type_counts.columns = ['Incident Type','Count'] 

                st.bar_chart(data=type_counts, x='Incident Type', y='Count', use_container_width=True)

                with table_col:
                    st.subheader('Real-Time Incident Tracker')
                    st.dataframe(df[['timestamp','prediction','log_text']].head(10),use_container_width=True, hide_index=True)

    time.sleep(2)
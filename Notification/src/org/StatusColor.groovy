package com.example.utils

class StatusColor {
    static String getColor(String buildStatus) {
        switch(buildStatus) {
            case 'ABORTED':
                return '#ffa500'
            case 'SUCCESS':
                return '#008000'
            case 'FAILURE':
                return '#ff0000'
            case 'UNSTABLE':
                return '#ffff00'
            default:
                return '#808080'
        }
    }
}

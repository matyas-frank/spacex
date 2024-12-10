package cz.frank.spacex.sharedtests

import cz.frank.spacex.launches.data.api.ILaunchesAPI
import cz.frank.spacex.launches.data.repository.toEntity
import cz.frank.spacex.launches.data.repository.toModel

class LaunchFactory {
    fun createLaunches(intRange: IntRange) = intRange.map {
        createLaunch(it).toEntity().toModel()
    }

    fun createLaunch(id: Int): ILaunchesAPI.LaunchPreviewResponse {
        return ILaunchesAPI.LaunchPreviewResponse(
            id.toString(),
            listOf("FalconSat", "FalconSat2").random(),
            links = ILaunchesAPI.LaunchPreviewResponse.Links(patch = ILaunchesAPI.LaunchPreviewResponse.Links.Patch(null)),
            rocket = ILaunchesAPI.LaunchPreviewResponse.Rocket("Falcon1"),
            upcoming = false,
            success = true
        )
    }
}

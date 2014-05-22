edu_uq_workways_ioportlet_mincviewer_MincClient = function() {
	var e = $(this.getElement());
	var mincviewerDiv = e.context;
	try{
		//loading div
		var loading_div = document.createElement('div'); 
		loading_div.id = 'loading';
		loading_div.style.display="none";
		var loading_img = document.createElement('img'); 
		loading_img.src="/IOPortlet/VAADIN/mincviewer/img/ajax-loader.gif";
		loading_div.appendChild(loading_img);   
		mincviewerDiv.appendChild(loading_div); 
		
		//divs
		var volume_ui_template_script = document.createElement('script'); 
		volume_ui_template_script.setAttribute('id', 'volume-ui-template');
		volume_ui_template_script.setAttribute('type', 'x-volume-ui-template');
		// TODO: there should be a better way
		var volume_ui_template_code = 
		   '<div class="volume-viewer-display"></div>										\
			<div class="volume-viewer-controls volume-controls">							\
			  <div class="coords">															\
			    <div class="control-heading" id="world-coordinates-heading-{{VOLID}}">		\
			      World Coordinates: 														\
			    </div>																		\
			    <div class="world-coords" data-volume-id="{{VOLID}}">						\
			      X:<input id="world-x-{{VOLID}}" class="control-inputs">					\
			      Y:<input id="world-y-{{VOLID}}" class="control-inputs">					\
			      Z:<input id="world-z-{{VOLID}}" class="control-inputs">					\
			    </div>																		\
			    <div class="control-heading" id="voxel-coordinates-heading-{{VOLID}}">		\
			      Voxel Coordinates: 														\
			    </div>																		\
			    <div class="voxel-coords" data-volume-id="{{VOLID}}">						\
			      X:<input id="voxel-x-{{VOLID}}" class="control-inputs">					\
			      Y:<input id="voxel-y-{{VOLID}}" class="control-inputs">					\
			      Z:<input id="voxel-z-{{VOLID}}" class="control-inputs">					\
			    </div>																		\
			  </div>																		\
			  <div id="color-map-{{VOLID}}">												\
			    <span class="control-heading" id="color-map-heading-{{VOLID}}">				\
			      Color Map: 																\
			    </span>																		\
			    <select class="color-map-select" data-volume-id="{{VOLID}}">				\
			      <option value="0" selected="">Spectral</option>							\
			      <option value="1">Thermal</option>										\
			      <option value="2">Gray</option>											\
			      <option value="3">Blue</option>											\
			      <option value="4">Green</option>											\
			    </select>																	\
			  </div>																		\
			  <div class="threshold-div" data-volume-id="{{VOLID}}">						\
			    <div class="control-heading">												\
			      Threshold: 																\
			    </div>																		\
			    <div class="thresh-inputs">													\
			      <input id="min-threshold-{{VOLID}}" class="control-inputs thresh-input-left" value="0"/>			\
			      <input id="max-threshold-{{VOLID}}" class="control-inputs thresh-input-right" value="255"/>		\
			    </div> 																								\
			    <div class="slider volume-viewer-threshold" id="threshold-slider-{{VOLID}}"></div>					\
			  </div>																								\
			  <div id="slice-series-{{VOLID}}" class="slice-series-div" data-volume-id="{{VOLID}}">					\
			    <!--div class="control-heading" id="slice-series-heading-{{VOLID}}">All slices: </div>					\
			    <span class="slice-series-button button" data-axis="xspace">Sagittal</span>							\
			    <span class="slice-series-button button" data-axis="yspace">Coronal</span>							\
			    <span class="slice-series-button button" data-axis="zspace">Transverse</span-->						\
			  </div>																								\
			</div>';
		try {
			volume_ui_template_script.appendChild(document.createTextNode(volume_ui_template_code));
	    } catch (e) {
	    	volume_ui_template_script.text = volume_ui_template_code;
	    }
	    //document.getElementsByTagName('body')[0].appendChild(volume_ui_template_script);
		mincviewerDiv.appendChild(volume_ui_template_script); 
		
		var overlay_ui_template_script = document.createElement('script'); 
		overlay_ui_template_script.setAttribute('id', 'overlay-ui-template');
		overlay_ui_template_script.setAttribute('type', 'x-volume-ui-template');
		var overlay_ui_template_code = 
			'<div class="overlay-viewer-display"></div>													\
			 <div class="volume-viewer-controls volume-controls"> 										\
			  <div class="coords">																		\
			  <div class="control-heading" id="world-coordinates-heading-{{VOLID}}">					\
				World Coordinates: 																		\
			    </div>																					\
			    <div class="world-coords" data-volume-id="{{VOLID}}">									\
			      X:<input id="world-x-{{VOLID}}" class="control-inputs">								\
			      Y:<input id="world-y-{{VOLID}}" class="control-inputs">								\
			      Z:<input id="world-z-{{VOLID}}" class="control-inputs">								\
			    </div>																					\
			    <div class="control-heading" id="voxel-coordinates-heading-{{VOLID}}">					\
			      Voxel Coordinates: 																	\
			    </div>																					\
			    <div class="voxel-coords" data-volume-id="{{VOLID}}">									\
			      X:<input id="voxel-x-{{VOLID}}" class="control-inputs">								\
			      Y:<input id="voxel-y-{{VOLID}}" class="control-inputs">								\
			      Z:<input id="voxel-z-{{VOLID}}" class="control-inputs">								\
			    </div>																					\
			  </div>																					\
			  <div class="blend-div" data-volume-id="{{VOLID}}">										\
			    <span class="control-heading" id="blend-heading{{VOLID}}">Blend (0.0 to 1.0):</span>	\
			    <input class="control-inputs" value="0.5" id="blend-val"/>								\
			    <div id="blend-slider" class="slider volume-viewer-blend"></div>						\
			  </div>  																					\
			</div>';
		try {
			overlay_ui_template_script.appendChild(document.createTextNode(overlay_ui_template_code));
	    } catch (e) {
	    	overlay_ui_template_script.text = overlay_ui_template_code;
	    }
		mincviewerDiv.appendChild(overlay_ui_template_script); 											
	    //document.getElementsByTagName('body')[0].appendChild(overlay_ui_template_script);
		
		//brainbrowser wrapper
		var brainbrowserwrapper_div = document.createElement('div'); 
		brainbrowserwrapper_div.id = 'brainbrowser-wrapper';
		brainbrowserwrapper_div.style.display="none";
		mincviewerDiv.appendChild(brainbrowserwrapper_div);
		//document.getElementsByTagName('body')[0].appendChild(brainbrowserwrapper_div);
		
		//volume-viewer
		var volumeviewer_div = document.createElement('div'); 
		volumeviewer_div.id = 'volume-viewer';
		brainbrowserwrapper_div.appendChild(volumeviewer_div);
		
		//brain browser
		var brainbrowser_div = document.createElement('div'); 
		brainbrowser_div.id = 'brainbrowser';
		volumeviewer_div.appendChild(brainbrowser_div);
	}catch(e) 
	{window.alert(e);};
	
	
	
	//global here
	var volumeList = null;
	//start function
	startVol = function(viewer){
		
		var loading_div = $("#loading");
		//even when viewer is ready
		viewer.addEventListener("ready", function() {
			  $(".button").button();

		      // Should cursors in all panels be synchronized?
		      $("#sync-volumes").change(function() {
		        viewer.synced = $(this).is(":checked");
		      });

		      // This will create an image of all the display panels
		      // currently being shown in the viewer.
		      $("#capture-slices").click(function() {
		        var width = viewer.displays[0][0].canvas.width;
		        var height = viewer.displays[0][0].canvas.height;
		        var active_canvas = viewer.active_canvas;
		        
		        // Create a canvas on which we'll draw the images.
		        var canvas = document.createElement("canvas");
		        var context = canvas.getContext("2d");
		        var img = new Image();
		        
		        canvas.width = width * viewer.displays.length;
		        canvas.height = height * 3;
		        context.fillStyle = "#000000";
		        context.fillRect(0, 0, canvas.width, canvas.height);
		        
		        // The active canvas is highlighted by the viewer,
		        // so we set it to null and redraw the highlighting
		        // isn't shown in the image.
		        viewer.active_canvas = null;
		        viewer.draw();
		        viewer.displays.forEach(function(display, x) {
		          display.forEach(function(panel, y) {
		            context.drawImage(panel.canvas, x * width, y * height);
		          });
		        });

		        // Restore the active canvas.
		        viewer.active_canvas = active_canvas;
		        viewer.draw();
		        
		        // Show the created image in a dialog box.
		        img.onload = function() {
		          $("<div></div>").append(img).dialog({
		            title: "Slices",
		            height: img.height,
		            width: img.width
		          });
		        };

		        img.src = canvas.toDataURL();
		      });

		      // The world coordinate input fields.
		      $(".world-coords").change(function() {
		        var div = $(this);

		        // Get the volume ID of the volume being displayed.
		        var vol_id = div.data("volume-id");


		        var x = parseFloat(div.find("#world-x-" + vol_id).val());
		        var y = parseFloat(div.find("#world-y-" + vol_id).val());
		        var z = parseFloat(div.find("#world-z-" + vol_id).val());
		        
		        // Make sure the values are numeric.
		        if (!BrainBrowser.utils.isNumeric(x)) {
		          x = 0;
		        }
		        if (!BrainBrowser.utils.isNumeric(y)) {
		          y = 0;
		        }
		        if (!BrainBrowser.utils.isNumeric(z)) {
		          z = 0;
		        }

		        // Set coordinates and redraw.
		        viewer.volumes[vol_id].setWorldCoords(x, y, z);

		        viewer.redrawVolumes();
		      });

		      // Change the color map currently being used to display data.
		      $(".color-map-select").change(function(event) {
		        var volume = viewer.volumes[$(this).data("volume-id")];
		        volume.color_map = BrainBrowser.VolumeViewer.color_maps[+$(event.target).val()];
		        viewer.redrawVolumes();
		      });

		      // Change the range of intensities that will be displayed.
		      $(".threshold-div").each(function() {
		        var div = $(this);
		        var vol_id = div.data("volume-id");
		        var volume = viewer.volumes[vol_id];

		        // Input fields to input min and max thresholds directly.
		        var min_input = div.find("#min-threshold-" + vol_id);
		        var max_input = div.find("#max-threshold-" + vol_id);

		        // Slider to modify min and max thresholds.
		        var slider = div.find(".slider");

		        slider.slider({
		          range: true,
		          min: 0,
		          max: 255,
		          values: [0, 255],
		          step: 1,
		          slide: function(event, ui){
		            var values = ui.values;

		            // Update the input fields.
		            min_input.val(values[0]);
		            max_input.val(values[1]);

		            // Update the volume and redraw.
		            volume.min = values[0];
		            volume.max = values[1];
		            viewer.redrawVolumes();
		          },
		          stop: function() {
		            $(this).find("a").blur();
		          }
		        });

		        // Input field for minimum threshold.
		        min_input.change(function() {
		          var value = parseFloat(this.value);
		          
		          // Make sure input is numeric and in range.
		          if (!BrainBrowser.utils.isNumeric(value)) {
		            value = 0;
		          }
		          value = Math.max(0, Math.min(value, 255));
		          this.value = value;

		          // Update the slider.
		          slider.slider("values", 0, value);

		          // Update the volume and redraw.
		          volume.min = value;
		          viewer.redrawVolumes();
		        });

		        max_input.change(function() {
		          var value = parseFloat(this.value);
		          
		          // Make sure input is numeric and in range.
		          if (!BrainBrowser.utils.isNumeric(value)) {
		            value = 255;
		          }
		          value = Math.max(0, Math.min(value, 255));
		          this.value = value;

		          // Update the slider.
		          slider.slider("values", 1, value);
		          
		          // Update the volume and redraw.
		          volume.max = value;
		          viewer.redrawVolumes();
		        });

		      });


		      // Blend controls for a multivolume overlay.
		      $(".blend-div").each(function() {
		        var div = $(this);
		        var slider = div.find(".slider");
		        var blend_input = div.find("#blend-val");

		        var vol_id = div.data("volume-id");
		        var volume = viewer.volumes[vol_id];

		        // Slider to select blend value.
		        slider.slider({
		          min: 0,
		          max: 1,
		          step: 0.01,
		          value: 0.5,
		          slide: function(event, ui) {
		            var value = parseFloat(ui.value);
		            volume.blend_ratios[0] = 1 - value;
		            volume.blend_ratios[1] = value;
		            


		            blend_input.val(value);
		            viewer.redrawVolumes();
		          },
		          stop: function() {
		            $(this).find("a").blur();
		          }
		        });
		        
		        // Input field to select blend values explicitly.
		        blend_input.change(function() {
		          var value = parseFloat(this.value);
		          
		          // Check that input is numeric and in range.
		          if (!BrainBrowser.utils.isNumeric(value)) {
		            value = 0;
		          }
		          value = Math.max(0, Math.min(value, 1));
		          this.value = value;

		          // Update slider and redraw volumes.
		          slider.slider("value", value);
		          volume.blend_ratios[0] = 1 - value;
		          volume.blend_ratios[1] = value;
		          viewer.redrawVolumes();
		        });
		      });


		      loading_div.hide();
		      $("#brainbrowser-wrapper").slideDown({duration: 600});
		      
		});//end viewer ready
		//slice updated
		viewer.addEventListener("sliceupdate", function() {
	      viewer.volumes.forEach(function(volume, vol_id) {
	        var world_coords = volume.getWorldCoords();
	        var voxel_coords = volume.getVoxelCoords();
	        $("#world-x-" + vol_id).val(world_coords.x.toPrecision(6));
	        $("#world-y-" + vol_id).val(world_coords.y.toPrecision(6));
	        $("#world-z-" + vol_id).val(world_coords.z.toPrecision(6));

	        $("#voxel-x-" + vol_id).val(voxel_coords.x.toPrecision(6));
	        $("#voxel-y-" + vol_id).val(voxel_coords.y.toPrecision(6));
	        $("#voxel-z-" + vol_id).val(voxel_coords.z.toPrecision(6));
	      });
	    });
		//show loading div
		loading_div.show();
		
		//load volumes
		viewer.loadVolumes({
			volumes: volumeList,
			panel_width: 256,
            panel_height: 256
		});
	};
	
	var firstLoad = true;
	this.onStateChange = function() {
		if(typeof this.getState().volumeList== 'undefined' || this.getState().volumeList.length == 0){
			//window.alert("volume list cannot be empty or null");
			return;
	  	}
		volumeList = this.getState().volumeList;
		console.log("volume list from statechange");
		console.log(volumeList);
		if(firstLoad){
			BrainBrowser.VolumeViewer.start("brainbrowser", startVol);
			firstLoad = false;
		}
		else{
			if(viewer){
				viewer.loadVolumes({
					volumes: volumeList,
					panel_width: 256,
		            panel_height: 256
				});
			}
		}
		
		
	};	//end of state change
};//end of com_example_...